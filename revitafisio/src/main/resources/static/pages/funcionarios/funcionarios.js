/**
 * @file Lógica para a página de gerenciamento de Equipe e Pacientes.
 * @description Controla a exibição de listas de funcionários e pacientes,
 * permitindo filtrar por status e realizar ações de ativação e inativação
 * utilizando um modal de confirmação customizado.
 */

/**
 * Função principal que é executada quando o DOM da página está completamente carregado.
 * É o ponto de entrada que configura toda a página.
 */
document.addEventListener('DOMContentLoaded', function() {
    // 1. Validação de Sessão e Controle de Acesso
    const dadosUsuario = JSON.parse(localStorage.getItem('usuarioLogado'));
    if (!dadosUsuario || !dadosUsuario.tipoUsuario.includes('ADMIN')) {
        // CORREÇÃO: Usa o novo modal para mensagens de erro, mantendo a consistência.
        showConfirmationModal('Acesso negado. Esta página é apenas para Administradores.', () => {
            window.location.href = '../dashboard/dashboard.html';
        });
        return; // Interrompe a execução se o usuário não for Admin.
    }

    // 2. Inicialização da Interface
    document.getElementById('userName').textContent = dadosUsuario.nome;
    renderizarSidebar(dadosUsuario.tipoUsuario);

    // 3. Listeners de Eventos para os botões de filtro
    document.getElementById('btn-funcionarios-ativos').addEventListener('click', () => carregarFuncionarios(true));
    document.getElementById('btn-funcionarios-inativos').addEventListener('click', () => carregarFuncionarios(false));
    document.getElementById('btn-pacientes-ativos').addEventListener('click', () => carregarPacientes(true));
    document.getElementById('btn-pacientes-inativos').addEventListener('click', () => carregarPacientes(false));

    // Listener para o botão de logout
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '../../login.html';
    });

    // 4. Carga Inicial dos Dados
    carregarFuncionarios(true);
    carregarPacientes(true);
});

/**
 * Busca funcionários (ativos ou inativos) na API e os renderiza na tabela.
 * @param {boolean} ativos - Se true, busca ativos; se false, busca inativos.
 */
async function carregarFuncionarios(ativos = true) {
    const endpoint = ativos ? '/funcionarios/ativos' : '/funcionarios/inativos';
    const tbody = document.getElementById('listaFuncionarios');
    tbody.innerHTML = `<tr><td colspan="3" class="text-center">Carregando...</td></tr>`;

    try {
        const response = await fetch(endpoint);
        if (!response.ok) throw new Error('Falha ao carregar funcionários.');

        const funcionarios = await response.json();
        tbody.innerHTML = ''; // Limpa a tabela

        if (funcionarios.length === 0) {
            tbody.innerHTML = `<tr><td colspan="3" class="text-center">Nenhum funcionário encontrado.</td></tr>`;
            return;
        }

        funcionarios.forEach(func => {
            const row = tbody.insertRow();
            const botaoAcao = ativos
                ? `<button onclick="inativarFuncionario(${func.id})" class="btn btn-sm btn-warning"><i class="fas fa-user-slash"></i> Inativar</button>`
                : `<button onclick="ativarFuncionario(${func.id})" class="btn btn-sm btn-success"><i class="fas fa-user-check"></i> Reativar</button>`;

            row.innerHTML = `
                <td>${func.nome}</td>
                <td>${func.tipo ? func.tipo.replace('_', ' ') : 'N/A'}</td>
                <td>
                    <a href="../prontuario-funcionario/prontuario-funcionario.html?id=${func.id}" class="btn btn-sm btn-info">
                        <i class="fas fa-eye"></i> Detalhes
                    </a>
                    ${botaoAcao}
                </td>
            `;
        });
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-danger">${error.message}</td></tr>`;
        console.error("Erro ao carregar funcionários:", error);
    }
}

/**
 * Busca pacientes (ativos ou inativos) na API e os renderiza na tabela.
 * @param {boolean} ativos - Se true, busca ativos; se false, busca inativos.
 */
async function carregarPacientes(ativos = true) {
    const endpoint = ativos ? '/pacientes' : '/pacientes/inativos';
    const tbody = document.getElementById('listaPacientes');
    tbody.innerHTML = `<tr><td colspan="3" class="text-center">Carregando...</td></tr>`;

    try {
        const response = await fetch(endpoint);
        if (!response.ok) throw new Error('Falha ao buscar pacientes.');

        const pacientes = await response.json();
        tbody.innerHTML = '';

        if (pacientes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="3" class="text-center">Nenhum paciente encontrado.</td></tr>`;
            return;
        }

        pacientes.forEach(paciente => {
            const row = tbody.insertRow();
            const botaoAcao = ativos
                ? `<button onclick="inativarPaciente(${paciente.id})" class="btn btn-sm btn-warning ml-2"><i class="fas fa-user-slash"></i> Inativar</button>`
                : `<button onclick="ativarPaciente(${paciente.id})" class="btn btn-sm btn-success ml-2"><i class="fas fa-user-check"></i> Reativar</button>`;

            row.innerHTML = `
                <td>${paciente.nome}</td>
                <td>${paciente.cpf}</td>
                <td>
                    <a href="../prontuario/prontuario.html?pacienteId=${paciente.id}" class="btn btn-sm btn-info">
                        <i class="fas fa-file-medical"></i> Prontuário
                    </a>
                    ${botaoAcao}
                </td>
            `;
        });
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-danger">${error.message}</td></tr>`;
        console.error("Erro ao carregar pacientes:", error);
    }
}

// --- Funções de Ação (Ativar/Inativar) Refatoradas ---

/**
 * Abre o modal de confirmação e, se confirmado, inativa o funcionário.
 * @param {number} id - O ID do funcionário.
 */
function inativarFuncionario(id) {
    showConfirmationModal('Tem certeza que deseja inativar este funcionário?', async () => {
        try {
            const response = await fetch(`/funcionarios/${id}/inativar`, { method: 'PATCH' });
            if (!response.ok) throw new Error('Falha ao inativar funcionário.');
            carregarFuncionarios(true);
        } catch (error) {
            showConfirmationModal(error.message, () => {}); // Mostra erro no modal
        }
    });
}

/**
 * Abre o modal de confirmação e, se confirmado, reativa o funcionário.
 * @param {number} id - O ID do funcionário.
 */
function ativarFuncionario(id) {
    showConfirmationModal('Tem certeza que deseja reativar este funcionário?', async () => {
        try {
            const response = await fetch(`/funcionarios/${id}/ativar`, { method: 'PATCH' });
            if (!response.ok) throw new Error('Falha ao reativar funcionário.');
            carregarFuncionarios(false);
        } catch (error) {
            showConfirmationModal(error.message, () => {});
        }
    });
}

/**
 * Abre o modal de confirmação e, se confirmado, inativa o paciente.
 * @param {number} id - O ID do paciente.
 */
function inativarPaciente(id) {
    showConfirmationModal('Tem certeza que deseja inativar este paciente?', async () => {
        try {
            const response = await fetch(`/pacientes/${id}/inativar`, { method: 'PATCH' });
            if (!response.ok) throw new Error('Falha ao inativar paciente.');
            carregarPacientes(true);
        } catch (error) {
            showConfirmationModal(error.message, () => {});
        }
    });
}

/**
 * Abre o modal de confirmação e, se confirmado, reativa o paciente.
 * @param {number} id - O ID do paciente.
 */
function ativarPaciente(id) {
    showConfirmationModal('Tem certeza que deseja reativar este paciente?', async () => {
        try {
            const response = await fetch(`/pacientes/${id}/ativar`, { method: 'PATCH' });
            if (!response.ok) throw new Error('Falha ao reativar paciente.');
            carregarPacientes(false);
        } catch (error) {
            showConfirmationModal(error.message, () => {});
        }
    });
}

// --- Funções de Renderização e Utilitários ---

/**
 * NOVA FUNÇÃO: Exibe um modal de confirmação genérico.
 * @param {string} message - A mensagem a ser exibida no corpo do modal.
 * @param {Function} onConfirm - A função (callback) a ser executada se o usuário clicar em "Confirmar".
 */
function showConfirmationModal(message, onConfirm) {
    const modal = $('#confirmationModal'); // Usa jQuery para selecionar o modal
    const modalBody = document.getElementById('confirmationModalBody');
    const confirmBtn = document.getElementById('confirmActionBtn');

    modalBody.textContent = message;

    // A parte mais importante: remove qualquer listener antigo e adiciona um novo.
    // O '.off("click")' é crucial para evitar que a ação seja executada múltiplas vezes.
    $(confirmBtn).off('click').on('click', function() {
        modal.modal('hide');
        onConfirm();
    });

    modal.modal('show');
}

/**
 * Renderiza os links do menu lateral com base no perfil do usuário.
 * @param {string} tipoUsuario - Os cargos do usuário logado.
 */
function renderizarSidebar(tipoUsuario) {
    const sidebarContainer = document.getElementById('sidebar-links');
    sidebarContainer.innerHTML = '';
    if (tipoUsuario.includes('ADMIN')) {
        // Marca o link "Gerenciar Equipe" como ativo nesta página.
        sidebarContainer.innerHTML += `<li class="nav-item active"><a class="nav-link" href="funcionarios.html"><i class="fas fa-fw fa-users-cog"></i><span>Gerenciar Equipe</span></a></li>`;
        sidebarContainer.innerHTML += `<li class="nav-item"><a class="nav-link" href="../relatorios/relatorios.html"><i class="fas fa-fw fa-chart-bar"></i><span>Relatórios</span></a></li>`;
    }
    if (tipoUsuario.includes('FISIOTERAPEUTA')) {
        sidebarContainer.innerHTML += `<li class="nav-item"><a class="nav-link" href="../meus-horarios/meus-horarios.html"><i class="fas fa-fw fa-clock"></i><span>Meus Horários</span></a></li>`;
    }
    sidebarContainer.innerHTML += `<li class="nav-item"><a class="nav-link" href="../agenda/agenda.html"><i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span></a></li>`;
}
