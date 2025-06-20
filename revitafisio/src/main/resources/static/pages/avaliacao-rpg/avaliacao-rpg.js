/**
 * @file Lógica para a Ficha de Avaliação de RPG.
 * @description Carrega dados do paciente e da avaliação, preenche o formulário e
 * gerencia o salvamento com validação, correção de dados e redirecionamento.
 */

// --- 1. CAPTURA DE DADOS INICIAIS ---
const urlParams = new URLSearchParams(window.location.search);
const pacienteId = urlParams.get('pacienteId');
const usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado'));

/**
 * Ponto de entrada: Roda quando o DOM da página está completamente carregado.
 */
document.addEventListener('DOMContentLoaded', function() {
    // --- 2. VERIFICAÇÃO DE SEGURANÇA ---
    // Protege a página, garantindo que apenas um fisioterapeuta logado
    // e com um ID de paciente válido possa acessá-la.
    if (!pacienteId || !usuarioLogado || !usuarioLogado.tipoUsuario.includes('FISIOTERAPEUTA')) {
        alert('Acesso inválido ou não autorizado.');
        window.location.href = '../dashboard/dashboard.html';
        return;
    }

    // --- 3. INICIALIZAÇÃO DA INTERFACE ---
    document.getElementById('userName').textContent = usuarioLogado.nome;
    document.getElementById('voltarProntuario').href = `../prontuario/prontuario.html?pacienteId=${pacienteId}`;
    renderizarSidebar();

    // --- 4. LISTENERS DE EVENTOS ---
    document.getElementById('formAvaliacaoRpg').addEventListener('submit', salvar);
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '../../login.html';
    });

    // Inicia o carregamento dos dados do paciente e da avaliação.
    carregarDados();
});

/**
 * Renderiza o menu lateral (sidebar).
 * Como esta é uma tela de sub-nível, apenas o link essencial da Agenda é exibido.
 */
function renderizarSidebar() {
    const sidebarContainer = document.getElementById('sidebar-links');
    sidebarContainer.innerHTML = `<li class="nav-item"><a class="nav-link" href="../agenda/agenda.html"><i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span></a></li>`;
}

/**
 * Carrega o nome do paciente para o cabeçalho e os dados da avaliação existente,
 * caso ela já tenha sido criada, para preencher o formulário.
 */
async function carregarDados() {
    try {
        const pacienteResponse = await fetch(`/pacientes/${pacienteId}`);
        const paciente = await pacienteResponse.json();
        document.getElementById('nomePaciente').textContent = paciente.nome;
    } catch (e) {
        document.getElementById('nomePaciente').textContent = "Paciente não encontrado";
        console.error("Erro ao buscar dados do paciente:", e);
    }

    try {
        const avaliacaoResponse = await fetch(`/avaliacoes/rpg/paciente/${pacienteId}`);
        if (avaliacaoResponse.ok) {
            const data = await avaliacaoResponse.json();
            preencherFormulario(data); // Chama a função para popular o formulário.
        } else {
            console.log("Nenhuma avaliação de RPG encontrada para este paciente.");
        }
    } catch (error) {
        console.error("Erro ao buscar avaliação de RPG:", error);
    }
}

/**
 * Preenche todos os campos do formulário com os dados de uma avaliação existente.
 * Lida com diferentes tipos de input (texto, checkbox, select).
 * @param {object} data - Objeto com os dados da avaliação vindos da API.
 */
function preencherFormulario(data) {
    for (const key in data) {
        // Garante que a propriedade pertence ao objeto e não é nula.
        if (data.hasOwnProperty(key) && data[key] !== null) {
            const element = document.getElementById(key);
            if (element) {
                // Tratamento especial para checkboxes.
                if (element.type === 'checkbox') {
                    element.checked = data[key];
                } else { // Para inputs de texto, textareas e selects.
                    element.value = data[key];
                }
            }
        }
    }
}

/**
 * Coleta os dados do formulário, valida, formata e envia para a API.
 * @param {Event} event - O evento de submissão do formulário.
 */
async function salvar(event) {
    event.preventDefault(); // Impede o recarregamento da página.
    const resultadoDiv = document.getElementById('resultado');
    resultadoDiv.className = 'alert alert-info';
    resultadoDiv.textContent = 'Salvando...';

    const form = document.getElementById('formAvaliacaoRpg');
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    // --- CORREÇÃO E VALIDAÇÃO DOS DADOS DO FORMULÁRIO ---
    // 1. Corrige os valores dos checkboxes: a FormData só envia 'on' se marcado.
    // É preciso verificar a propriedade '.checked' para garantir o envio de true/false.
    data.ressonancia_magnetica = document.getElementById('ressonancia_magnetica').checked;
    data.raio_x = document.getElementById('raio_x').checked;
    data.tomografia = document.getElementById('tomografia').checked;
    data.uso_medicamentos = document.getElementById('uso_medicamentos').checked;

    // 2. Corrige os valores dos campos <select>. Se um 'select' não for escolhido,
    // ele envia uma string vazia (""), o que causaria um erro 400 no backend ao tentar
    // converter para um Enum. Aqui, convertemos a string vazia para 'null'.
    const enumFields = ['grau_dor', 'cabeca', 'ombros', 'maos', 'eias', 'joelhos', 'lombar', 'pelve', 'escapulas'];
    for (const field of enumFields) {
        if (data[field] === "") {
            data[field] = null;
        }
    }

    // Adiciona os IDs do paciente e do fisioterapeuta logado, que não estão no formulário.
    data.idPaciente = parseInt(pacienteId);
    data.idFisioterapeuta = usuarioLogado.usuarioId;

    try {
        // Envia a requisição POST para o backend.
        const response = await fetch('/avaliacoes/rpg', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        // Trata a resposta do servidor.
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.message || 'Falha ao salvar a avaliação de RPG.');
        }

        resultadoDiv.className = 'alert alert-success';
        resultadoDiv.textContent = 'Avaliação salva com sucesso! Redirecionando...';

        // Redireciona o usuário de volta para o prontuário após 2 segundos.
        setTimeout(() => {
            window.location.href = `../prontuario/prontuario.html?pacienteId=${pacienteId}`;
        }, 2000);

    } catch (error) {
        // Captura e exibe erros, sejam de validação do backend ou de rede.
        resultadoDiv.className = 'alert alert-danger';
        resultadoDiv.textContent = error.message;
        console.error("Erro ao salvar avaliação de RPG:", error);
    }
}
