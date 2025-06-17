/**
 * @file Lógica para a página de gerenciamento de Equipe e Pacientes.
 * @description Controla a exibição de listas de funcionários e pacientes,
 * permitindo filtrar por status (ativos/inativos) e realizar ações de
 * ativação e inativação.
 */

document.addEventListener('DOMContentLoaded', function() {
    const dadosUsuario = JSON.parse(localStorage.getItem('usuarioLogado'));

    // Proteção de Rota: Apenas Admins podem acessar
    if (!dadosUsuario || !dadosUsuario.tipoUsuario.includes('ADMIN')) {
        alert('Acesso negado. Esta página é apenas para Administradores.');
        window.location.href = '../dashboard/dashboard.html';
        return;
    }

    // Preenche dados do usuário logado e menu lateral
    document.getElementById('userName').textContent = dadosUsuario.nome;
    renderizarSidebar(dadosUsuario.tipoUsuario);

    // Adiciona os eventos de clique aos botões de filtro de FUNCIONÁRIOS
    document.getElementById('btn-funcionarios-ativos').addEventListener('click', () => carregarFuncionarios(true));
    document.getElementById('btn-funcionarios-inativos').addEventListener('click', () => carregarFuncionarios(false));

    // Adiciona os eventos de clique aos botões de filtro de PACIENTES
    document.getElementById('btn-pacientes-ativos').addEventListener('click', () => carregarPacientes(true));
    document.getElementById('btn-pacientes-inativos').addEventListener('click', () => carregarPacientes(false));

    // Configura o botão de logout
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '../../login.html';
    });

    // Carga inicial das listas
    carregarFuncionarios(true); // Carrega funcionários ativos por padrão
    carregarPacientes(true);  // Carrega pacientes ativos por padrão
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
            // O botão de ação muda dependendo do status do funcionário
            const botaoAcao = ativos ?
                `<button onclick="inativarFuncionario(${func.id})" class="btn btn-sm btn-warning"><i class="fas fa-user-slash"></i> Inativar</button>` :
                `<button onclick="ativarFuncionario(${func.id})" class="btn btn-sm btn-success"><i class="fas fa-user-check"></i> Reativar</button>`;

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
            // O botão de ação muda dependendo do status do paciente
            const botaoAcao = ativos ?
                `<button onclick="inativarPaciente(${paciente.id})" class="btn btn-sm btn-warning ml-2"><i class="fas fa-user-slash"></i> Inativar</button>` :
                `<button onclick="ativarPaciente(${paciente.id})" class="btn btn-sm btn-success ml-2"><i class="fas fa-user-check"></i> Reativar</button>`;

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
    }
}

// --- Funções de Ação ---

async function inativarFuncionario(id) {
    if (!confirm('Tem certeza que deseja inativar este funcionário?')) return;
    try {
        const response = await fetch(`/funcionarios/${id}/inativar`, { method: 'PATCH' });
        if (!response.ok) throw new Error('Falha ao inativar funcionário.');
        carregarFuncionarios(true);
    } catch (error) {
        alert(error.message);
    }
}

async function ativarFuncionario(id) {
    if (!confirm('Tem certeza que deseja reativar este funcionário?')) return;
    try {
        const response = await fetch(`/funcionarios/${id}/ativar`, { method: 'PATCH' });
        if (!response.ok) throw new Error('Falha ao reativar funcionário.');
        carregarFuncionarios(false);
    } catch (error) {
        alert(error.message);
    }
}

async function inativarPaciente(id) {
    if (!confirm('Tem certeza que deseja inativar este paciente?')) return;
    try {
        const response = await fetch(`/pacientes/${id}/inativar`, { method: 'PATCH' });
        if (!response.ok) throw new Error('Falha ao inativar paciente.');
        carregarPacientes(true);
    } catch (error) {
        alert(error.message);
    }
}

async function ativarPaciente(id) {
    if (!confirm('Tem certeza que deseja reativar este paciente?')) return;
    try {
        const response = await fetch(`/pacientes/${id}/ativar`, { method: 'PATCH' });
        if (!response.ok) throw new Error('Falha ao reativar paciente.');
        carregarPacientes(false);
    } catch (error) {
        alert(error.message);
    }
}

// --- Funções de Renderização do Layout ---

function renderizarSidebar(tipoUsuario) {
    const sidebarContainer = document.getElementById('sidebar-links');
    sidebarContainer.innerHTML = '';
    if (tipoUsuario.includes('ADMIN')) {
        sidebarContainer.innerHTML += `<li class="nav-item active"><a class="nav-link" href="funcionarios.html"><i class="fas fa-fw fa-users-cog"></i><span>Gerenciar Equipe</span></a></li>`;
        sidebarContainer.innerHTML += `<li class="nav-item"><a class="nav-link" href="../relatorios/relatorios.html"><i class="fas fa-fw fa-chart-bar"></i><span>Relatórios</span></a></li>`;
    }
    if (tipoUsuario.includes('FISIOTERAPEUTA')) {
        sidebarContainer.innerHTML += `<li class="nav-item"><a class="nav-link" href="../meus-horarios/meus-horarios.html"><i class="fas fa-fw fa-clock"></i><span>Meus Horários</span></a></li>`;
    }
    sidebarContainer.innerHTML += `<li class="nav-item"><a class="nav-link" href="../agenda/agenda.html"><i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span></a></li>`;
}
