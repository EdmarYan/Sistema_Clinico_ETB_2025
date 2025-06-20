/**
 * @file Lógica para a Ficha de Avaliação de Ortopedia.
 * @description Este script gerencia a busca de dados do paciente e da avaliação,
 * o preenchimento automático do formulário com dados existentes e o salvamento
 * das informações, com redirecionamento em caso de sucesso.
 */

// --- 1. CAPTURA DE DADOS INICIAIS ---
// Extrai o ID do paciente da URL (ex: ?pacienteId=123).
const urlParams = new URLSearchParams(window.location.search);
const pacienteId = urlParams.get('pacienteId');
// Recupera os dados do usuário logado do localStorage.
const usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado'));

/**
 * Ponto de entrada do script. Roda assim que o HTML da página é carregado.
 */
document.addEventListener('DOMContentLoaded', function() {
    // --- 2. VERIFICAÇÃO DE SEGURANÇA ---
    // Se não houver pacienteId, usuário logado ou se o usuário não for um Fisioterapeuta,
    // o acesso é bloqueado e o usuário é redirecionado.
    if (!pacienteId || !usuarioLogado || !usuarioLogado.tipoUsuario.includes('FISIOTERAPEUTA')) {
        alert('Acesso inválido ou não autorizado.');
        window.location.href = '../dashboard/dashboard.html';
        return; // Interrompe a execução do script.
    }

    // --- 3. INICIALIZAÇÃO DA INTERFACE ---
    // Preenche os elementos básicos da página (menu, nome do usuário, etc.).
    document.getElementById('userName').textContent = usuarioLogado.nome;
    document.getElementById('voltarProntuario').href = `../prontuario/prontuario.html?pacienteId=${pacienteId}`;
    renderizarSidebar(usuarioLogado.tipoUsuario);

    // --- 4. LISTENERS DE EVENTOS ---
    // Adiciona o listener para o envio (submit) do formulário.
    document.getElementById('formAvaliacaoOrtopedia').addEventListener('submit', salvar);
    // Adiciona o listener para o botão de logout.
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '../../login.html';
    });

    // Inicia o processo de carregar os dados do paciente e da avaliação.
    carregarDados();
});

/**
 * Renderiza o menu lateral de acordo com o perfil do usuário.
 * @param {string} tipoUsuario - Cargos do usuário logado.
 */
function renderizarSidebar(tipoUsuario) {
    const sidebarContainer = document.getElementById('sidebar-links');
    // Como esta é uma tela interna, o menu é simplificado, contendo apenas o link para a Agenda.
    sidebarContainer.innerHTML = `<li class="nav-item"><a class="nav-link" href="../agenda/agenda.html"><i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span></a></li>`;
}

/**
 * Carrega o nome do paciente para o cabeçalho e os dados da avaliação
 * existente (se houver) para preencher o formulário.
 */
async function carregarDados() {
    // Busca e preenche o nome do paciente.
    try {
        const pacienteResponse = await fetch(`/pacientes/${pacienteId}`);
        const paciente = await pacienteResponse.json();
        document.getElementById('nomePaciente').textContent = paciente.nome;
    } catch(e) {
        document.getElementById('nomePaciente').textContent = "Paciente não encontrado";
        console.error("Erro ao buscar dados do paciente:", e);
    }

    // Busca a avaliação de ortopedia. Se existir, preenche o formulário.
    try {
        const avaliacaoResponse = await fetch(`/avaliacoes/ortopedia/paciente/${pacienteId}`);
        if (avaliacaoResponse.ok) {
            const data = await avaliacaoResponse.json();
            // Loop inteligente que preenche cada campo do formulário cujo 'id'
            // corresponde a uma chave no objeto de dados recebido da API.
            for (const key in data) {
                if (data.hasOwnProperty(key)) {
                    const element = document.getElementById(key);
                    if (element) {
                        element.value = data[key];
                    }
                }
            }
        } else {
            console.log("Nenhuma avaliação de ortopedia encontrada para este paciente. Exibindo formulário em branco.");
        }
    } catch(error) {
        console.error("Erro ao buscar avaliação de ortopedia:", error);
    }
}

/**
 * Coleta os dados do formulário, constrói o objeto de requisição,
 * envia para a API e trata a resposta.
 * @param {Event} event - O evento de submissão do formulário.
 */
async function salvar(event) {
    event.preventDefault(); // Impede o recarregamento da página.
    const resultadoDiv = document.getElementById('resultado');
    resultadoDiv.className = 'alert alert-info';
    resultadoDiv.textContent = 'Salvando...';

    // Coleta todos os dados do formulário de uma vez.
    const form = document.getElementById('formAvaliacaoOrtopedia');
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    // Adiciona os IDs do paciente e do fisioterapeuta logado, que não estão no formulário.
    data.idPaciente = parseInt(pacienteId);
    data.idFisioterapeuta = usuarioLogado.usuarioId;

    try {
        // Envia a requisição POST para o backend.
        const response = await fetch('/avaliacoes/ortopedia', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        // Se a resposta não for 'ok' (status diferente de 2xx), trata como um erro.
        if (!response.ok) {
            const erro = await response.json();
            // Lança um erro com a mensagem vinda do backend para ser capturado pelo bloco catch.
            throw new Error(erro.message || 'Falha ao salvar avaliação.');
        }

        // Se a operação for bem-sucedida.
        resultadoDiv.className = 'alert alert-success';
        resultadoDiv.textContent = 'Avaliação salva com sucesso! Redirecionando para o prontuário...';

        // Redireciona o usuário de volta para o prontuário após 2 segundos.
        setTimeout(() => {
            window.location.href = `../prontuario/prontuario.html?pacienteId=${pacienteId}`;
        }, 2000);

    } catch(error) {
        // Captura erros (tanto da validação do 'throw new Error' quanto de rede).
        resultadoDiv.className = 'alert alert-danger';
        resultadoDiv.textContent = error.message;
        console.error("Erro ao salvar avaliação:", error);
    }
}

