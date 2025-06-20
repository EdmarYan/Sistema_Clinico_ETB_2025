/**
 * Arquivo: js/script.js
 * Descrição: Contém toda a lógica de frontend para a página de login, incluindo
 * máscara de CPF, visualização de senha, validação de formulário e a requisição
 * para a API de autenticação.
 */

// Garante que tod0 o script seja executado apenas após o carregamento completo do HTML.
document.addEventListener('DOMContentLoaded', function() {

    // --- LÓGICA DA MÁSCARA DE CPF ---
    const cpfInput = document.getElementById('cpf');
    if (cpfInput) {
        cpfInput.addEventListener('input', (event) => {
            // Remove tudo que não é dígito para limpar o valor.
            let value = event.target.value.replace(/\D/g, '');

            // Limita o tamanho para 11 dígitos, o tamanho real de um CPF.
            if (value.length > 11) {
                value = value.substring(0, 11);
            }

            // Aplica a máscara (###.###.###-##) dinamicamente enquanto o usuário digita.
            if (value.length > 9) {
                value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
            } else if (value.length > 6) {
                value = value.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
            } else if (value.length > 3) {
                value = value.replace(/(\d{3})(\d{1,3})/, '$1.$2');
            }

            event.target.value = value;
        });
    }

    // --- LÓGICA DE MOSTRAR/ESCONDER SENHA ---
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('senha');
    const eyeIcon = document.getElementById('eyeIcon');
    const eyeSlashIcon = document.getElementById('eyeSlashIcon');

    if (togglePassword) {
        togglePassword.addEventListener('click', () => {
            // Verifica o tipo atual do input de senha.
            const isPassword = passwordInput.type === 'password';
            // Alterna o tipo do input entre 'password' e 'text'.
            passwordInput.type = isPassword ? 'text' : 'password';
            // Alterna a visibilidade dos ícones de olho aberto/fechado.
            eyeIcon.classList.toggle('hidden', isPassword);
            eyeSlashIcon.classList.toggle('hidden', !isPassword);
        });
    }

    // --- LÓGICA DE SUBMISSÃO DO FORMULÁRIO ---
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        // Adiciona um listener para o evento de 'submit' do formulário.
        loginForm.addEventListener('submit', function(event) {
            event.preventDefault(); // Impede o recarregamento padrão da página.
            fazerLogin(); // Chama a nossa função de login assíncrona.
        });
    }
});

/**
 * Função assíncrona que executa o processo completo de login:
 * 1. Coleta e valida os dados do formulário.
 * 2. Mostra um feedback visual de carregamento.
 * 3. Envia a requisição para a API de backend.
 * 4. Trata a resposta (sucesso ou erro).
 * 5. Restaura o estado do botão.
 */
async function fazerLogin() {
    // 1. Coleta os elementos e valores do formulário.
    const cpf = document.getElementById('cpf').value;
    const senha = document.getElementById('senha').value;
    const resultadoDiv = document.getElementById('resultado');
    const loginButton = document.getElementById('loginButton');
    const buttonText = document.getElementById('buttonText');
    const buttonSpinner = document.getElementById('buttonSpinner');

    // 2. Reseta as mensagens de erro/sucesso anteriores.
    resultadoDiv.textContent = '';
    resultadoDiv.className = '';

    // 3. Validação de Frontend: impede o envio de dados inválidos para a API.
    const cpfNumerico = cpf.replace(/\D/g, ''); // Remove a máscara para validação.
    if (cpfNumerico.length !== 11) {
        resultadoDiv.className = 'error-message';
        resultadoDiv.textContent = 'Por favor, informe um CPF válido com 11 dígitos.';
        return; // Interrompe a execução se o CPF for inválido.
    }
    if (senha.length < 4) {
        resultadoDiv.className = 'error-message';
        resultadoDiv.textContent = 'A senha deve ter pelo menos 4 caracteres.';
        return; // Interrompe a execução se a senha for muito curta.
    }

    // 4. Feedback Visual: Mostra o spinner e desabilita o botão para evitar cliques múltiplos.
    loginButton.disabled = true;
    buttonText.classList.add('hidden');
    buttonSpinner.classList.remove('hidden');

    // 5. Prepara o corpo da requisição em formato JSON.
    const dados = { cpf: cpfNumerico, senha: senha };

    // 6. Bloco try...catch...finally para a chamada da API.
    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados)
        });

        // 7. Tratamento da Resposta do Servidor.
        if (response.ok) { // Status HTTP na faixa de 200-299 (sucesso).
            const dadosUsuario = await response.json();
            // Salva os dados do usuário no localStorage para manter a sessão.
            localStorage.setItem('usuarioLogado', JSON.stringify(dadosUsuario));
            window.location.href = 'pages/dashboard/dashboard.html'; // Redireciona para o painel.

        } else { // Status HTTP de erro (4xx ou 5xx).
            const erro = await response.json();
            resultadoDiv.className = 'error-message';
            resultadoDiv.textContent = `Erro: ${erro.message || 'CPF ou senha inválidos.'}`;
        }
    } catch (error) { // Erro de rede (ex: servidor offline).
        resultadoDiv.className = 'error-message';
        resultadoDiv.textContent = 'Erro de conexão. Não foi possível conectar ao servidor.';
        console.error('Erro no login:', error);
    } finally {
        // 8. Este bloco sempre será executado, restaurando o botão ao seu estado original.
        loginButton.disabled = false;
        buttonText.classList.remove('hidden');
        buttonSpinner.classList.add('hidden');
    }
}
