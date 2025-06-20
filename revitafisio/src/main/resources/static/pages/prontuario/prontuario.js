/**
 * @file Lógica completa para a página de prontuário do paciente.
 * @description Gerencia a exibição de dados do paciente, histórico de evoluções,
 * avaliações clínicas e operações de edição/inativação.
 * @version 2.0
 */

// =======================================================
// VARIÁVEIS GLOBAIS E CONSTANTES
// =======================================================
let pacienteAtual = {};
let usuarioLogado;
const LOADING_DELAY = 300; // Delay para feedback visual

// =======================================================
// FUNÇÕES DE INICIALIZAÇÃO
// =======================================================

/**
 * Ponto de entrada principal - executado quando o DOM estiver carregado
 */
document.addEventListener('DOMContentLoaded', function() {
    try {
        inicializarPagina();
    } catch (error) {
        console.error("Erro crítico na inicialização:", error);
        mostrarErro("Falha grave no sistema. Recarregue a página.");
    }
});

/**
 * Inicializa a página e configura os listeners
 */
function inicializarPagina() {
    const urlParams = new URLSearchParams(window.location.search);
    const pacienteId = urlParams.get('pacienteId');

    // Validação básica de segurança
    if (!pacienteId || isNaN(pacienteId)) {
        mostrarErro("ID do paciente inválido na URL");
        return;
    }

    usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado')) || {};

    // Verificação de permissão
    if (!usuarioLogado || !usuarioLogado.tipoUsuario) {
        mostrarErro("Sessão expirada. Redirecionando para login...", () => {
            window.location.href = '../../login.html';
        });
        return;
    }

    // Configuração inicial
    configurarInterfaceUsuario();
    carregarDadosPaciente(pacienteId);
    configurarEventListeners();
}

/**
 * Configura elementos visuais da interface
 */
function configurarInterfaceUsuario() {
    try {
        document.getElementById('userName').textContent = usuarioLogado.nome || 'Usuário';
        renderizarSidebar();
        configurarVisibilidadePorPerfil();
    } catch (error) {
        console.error("Erro na configuração da interface:", error);
    }
}

/**
 * Configura listeners de eventos
 */
function configurarEventListeners() {
    // Logout
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '../../login.html';
    });

    // Formulário de evolução
    const evolucaoForm = document.getElementById('evolucaoForm');
    if (evolucaoForm) {
        evolucaoForm.addEventListener('submit', (e) => {
            e.preventDefault();
            salvarEvolucao();
        });
    }

    // Botão de inativação/ativação (adicionado dinamicamente)
    document.addEventListener('click', function(e) {
        if (e.target.id === 'inativarBtn') {
            inativarPaciente(pacienteAtual.id);
        }
        if (e.target.id === 'ativarBtn') {
            ativarPaciente(pacienteAtual.id);
        }
    });
}

// =======================================================
// FUNÇÕES DE RENDERIZAÇÃO
// =======================================================

/**
 * Renderiza a sidebar com base no perfil do usuário
 */
function renderizarSidebar() {
    try {
        const sidebarContainer = document.getElementById('sidebar-links');
        if (!sidebarContainer) return;

        sidebarContainer.innerHTML = '';

        // ADMIN
        if (usuarioLogado.tipoUsuario.includes('ADMIN')) {
            sidebarContainer.innerHTML += `
                <li class="nav-item">
                    <a class="nav-link" href="../funcionarios/funcionarios.html">
                        <i class="fas fa-fw fa-users-cog"></i><span>Gerenciar Equipe</span>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="../relatorios/relatorios.html">
                        <i class="fas fa-fw fa-chart-bar"></i><span>Relatórios</span>
                    </a>
                </li>`;
        }

        // FISIOTERAPEUTA
        if (usuarioLogado.tipoUsuario.includes('FISIOTERAPEUTA')) {
            sidebarContainer.innerHTML += `
                <li class="nav-item">
                    <a class="nav-link" href="../meus-horarios/meus-horarios.html">
                        <i class="fas fa-fw fa-clock"></i><span>Meus Horários</span>
                    </a>
                </li>`;
        }

        // Todos os funcionários
        sidebarContainer.innerHTML += `
            <li class="nav-item">
                <a class="nav-link" href="../agenda/agenda.html">
                    <i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span>
                </a>
            </li>`;
    } catch (error) {
        console.error("Erro ao renderizar sidebar:", error);
    }
}

/**
 * Configura a visibilidade dos elementos baseada no perfil
 */
function configurarVisibilidadePorPerfil() {
    try {
        const isFisio = usuarioLogado.tipoUsuario.includes('FISIOTERAPEUTA');
        const isAdmin = usuarioLogado.tipoUsuario.includes('ADMIN');

        // Mostrar/ocultar seções específicas
        document.getElementById('card-evolucao-form').style.display = isFisio ? 'block' : 'none';
        document.getElementById('card-avaliacoes').style.display = isFisio ? 'block' : 'none';
        document.getElementById('botoes-edicao-paciente').style.display = isAdmin ? 'block' : 'none';
    } catch (error) {
        console.error("Erro na configuração de perfil:", error);
    }
}

/**
 * Preenche os dados do paciente na interface
 * @param {Object} paciente - Dados do paciente
 */
function preencherDadosPaciente(paciente) {
    try {
        document.getElementById('nomePacienteView').textContent = `Prontuário de: ${paciente.nome}`;

        // Formatar contatos
        let contatosHTML = '<div class="mt-3"><h6 class="font-weight-bold text-primary">Contatos</h6>';
        if (paciente.contatos?.length > 0) {
            paciente.contatos.forEach(contato => {
                const tipo = contato.tipo.charAt(0).toUpperCase() + contato.tipo.slice(1).toLowerCase();
                contatosHTML += `<p class="mb-1"><strong>${tipo}:</strong> ${contato.valor}</p>`;
            });
        } else {
            contatosHTML += '<p class="text-muted">Nenhum contato cadastrado</p>';
        }
        contatosHTML += '</div>';

        // View Mode
        document.getElementById('viewMode').innerHTML = `
            <p class="mb-1"><strong>CPF:</strong> ${paciente.cpf}</p>
            <p class="mb-1"><strong>Nascimento:</strong> ${formatarData(paciente.dataNascimento)}</p>
            <p class="mb-0"><strong>Status:</strong> ${renderizarStatus(paciente.ativo)}</p>
            ${contatosHTML}
        `;

        // Edit Mode
        document.getElementById('editMode').innerHTML = `
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="nomeInput" class="form-label">Nome</label>
                    <input type="text" class="form-control" id="nomeInput" value="${paciente.nome}" required>
                </div>
                <div class="col-md-6 mb-3">
                    <label for="nascimentoInput" class="form-label">Nascimento</label>
                    <input type="date" class="form-control" id="nascimentoInput" value="${paciente.dataNascimento}" required>
                </div>
            </div>
        `;

        // Botões de ação
        document.getElementById('botoes-edicao-paciente').innerHTML = `
            <button class="btn btn-sm btn-secondary" id="editButton">
                <i class="fas fa-edit"></i> Editar
            </button>
            <button class="btn btn-sm btn-success d-none" id="saveButton">
                <i class="fas fa-save"></i> Salvar
            </button>
            <button class="btn btn-sm btn-light d-none" id="cancelButton">
                Cancelar
            </button>
            ${paciente.ativo ? `
            <button class="btn btn-sm btn-danger" id="inativarBtn">
                <i class="fas fa-trash"></i> Inativar
            </button>` : `
            <button class="btn btn-sm btn-success" id="ativarBtn">
                <i class="fas fa-check"></i> Reativar
            </button>`}
        `;

        // Configurar eventos dos botões
        document.getElementById('editButton').addEventListener('click', () => toggleEditMode(true));
        document.getElementById('saveButton').addEventListener('click', () => salvarAlteracoes(paciente.id));
        document.getElementById('cancelButton').addEventListener('click', () => toggleEditMode(false));

    } catch (error) {
        console.error("Erro ao preencher dados do paciente:", error);
        mostrarErro("Falha ao carregar dados do paciente");
    }
}

// =======================================================
// FUNÇÕES DE CONTROLE DE INTERFACE
// =======================================================

/**
 * Alterna entre modo de visualização e edição
 * @param {boolean} editing - True para modo edição
 */
function toggleEditMode(editing) {
    document.getElementById('viewMode').classList.toggle('d-none', editing);
    document.getElementById('editMode').classList.toggle('d-none', !editing);
    document.getElementById('editButton').classList.toggle('d-none', editing);
    document.getElementById('saveButton').classList.toggle('d-none', !editing);
    document.getElementById('cancelButton').classList.toggle('d-none', !editing);
}

/**
 * Mostra feedback de carregamento
 */
function mostrarLoading() {
    document.getElementById('loading').classList.remove('d-none');
    document.getElementById('patient-details').classList.add('d-none');
}

/**
 * Esconde feedback de carregamento
 */
function esconderLoading() {
    setTimeout(() => {
        document.getElementById('loading').classList.add('d-none');
        document.getElementById('patient-details').classList.remove('d-none');
    }, LOADING_DELAY);
}

/**
 * Mostra mensagem de erro
 * @param {string} mensagem - Mensagem de erro
 * @param {function} callback - Função a ser executada após o erro
 */
function mostrarErro(mensagem, callback) {
    alert(mensagem);
    if (callback && typeof callback === 'function') callback();
}

// =======================================================
// FUNÇÕES DE INTERAÇÃO COM API
// =======================================================

/**
 * Carrega dados do paciente
 * @param {number} id - ID do paciente
 */
async function carregarDadosPaciente(id) {
    try {
        mostrarLoading();

        const response = await fetch(`/pacientes/${id}`);

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao carregar paciente');
        }

        pacienteAtual = await response.json();
        preencherDadosPaciente(pacienteAtual);

        // Carregar dados complementares
        await Promise.all([
            carregarEvolucoes(id),
            carregarStatusAvaliacoes(id)
        ]);

    } catch (error) {
        console.error("Erro ao carregar paciente:", error);
        mostrarErro(error.message, () => {
            window.location.href = '../dashboard/dashboard.html';
        });
    } finally {
        esconderLoading();
    }
}

/**
 * Carrega o histórico de evoluções
 * @param {number} idPaciente - ID do paciente
 */
async function carregarEvolucoes(idPaciente) {
    try {
        const historicoDiv = document.getElementById('historicoEvolucoes');
        historicoDiv.innerHTML = '<div class="text-center"><div class="spinner-border spinner-border-sm"></div></div>';

        const response = await fetch(`/evolucoes/paciente/${idPaciente}`);

        if (!response.ok) {
            throw new Error('Falha ao carregar evoluções');
        }

        const evolucoes = await response.json();

        if (evolucoes.length === 0) {
            historicoDiv.innerHTML = '<p class="text-muted text-center">Nenhuma evolução registrada</p>';
            return;
        }

        historicoDiv.innerHTML = evolucoes.map(evo => {
            const dataFormatada = formatarData(evo.data);
            return `
                <div class="evolution-entry mb-3 p-3 border rounded">
                    <p class="mb-1">${evo.descricao.replace(/\n/g, '<br>')}</p>
                    <small class="text-muted d-block mt-2">
                        <i class="fas fa-user-md"></i> ${evo.nomeFisioterapeuta} 
                        <i class="fas fa-calendar ml-2"></i> ${dataFormatada}
                    </small>
                </div>
            `;
        }).join('');

    } catch (error) {
        console.error("Erro ao carregar evoluções:", error);
        document.getElementById('historicoEvolucoes').innerHTML = `
            <p class="text-danger text-center">
                <i class="fas fa-exclamation-triangle"></i> Falha ao carregar histórico
            </p>
        `;
    }
}

/**
 * Carrega status das avaliações
 * @param {number} idPaciente - ID do paciente
 */
async function carregarStatusAvaliacoes(idPaciente) {
    try {
        const [resOrto, resRpg] = await Promise.all([
            fetch(`/avaliacoes/ortopedia/paciente/${idPaciente}`),
            fetch(`/avaliacoes/rpg/paciente/${idPaciente}`)
        ]);

        const containerOrto = document.getElementById('container-avaliacao-ortopedia');
        const containerRpg = document.getElementById('container-avaliacao-rpg');

        // Ortopedia
        if (resOrto.ok) {
            containerOrto.innerHTML = `
                <span>Avaliação de Ortopedia</span>
                <a href="../avaliacao-ortopedia/avaliacao-ortopedia.html?pacienteId=${idPaciente}" 
                   class="btn btn-sm btn-outline-primary">
                   <i class="fas fa-edit"></i> Ver/Editar
                </a>
            `;
        } else {
            containerOrto.innerHTML = `
                <span>Avaliação de Ortopedia</span>
                <a href="../avaliacao-ortopedia/avaliacao-ortopedia.html?pacienteId=${idPaciente}" 
                   class="btn btn-sm btn-primary">
                   <i class="fas fa-plus"></i> Realizar
                </a>
            `;
        }

        // RPG
        if (resRpg.ok) {
            containerRpg.innerHTML = `
                <span>Avaliação de RPG</span>
                <a href="../avaliacao-rpg/avaliacao-rpg.html?pacienteId=${idPaciente}" 
                   class="btn btn-sm btn-outline-success">
                   <i class="fas fa-edit"></i> Ver/Editar
                </a>
            `;
        } else {
            containerRpg.innerHTML = `
                <span>Avaliação de RPG</span>
                <a href="../avaliacao-rpg/avaliacao-rpg.html?pacienteId=${idPaciente}" 
                   class="btn btn-sm btn-success">
                   <i class="fas fa-plus"></i> Realizar
                </a>
            `;
        }

    } catch (error) {
        console.error("Erro ao carregar avaliações:", error);
        document.getElementById('container-avaliacao-ortopedia').innerHTML = `
            <span class="text-danger">Erro ao carregar</span>
        `;
        document.getElementById('container-avaliacao-rpg').innerHTML = `
            <span class="text-danger">Erro ao carregar</span>
        `;
    }
}

/**
 * Salva uma nova evolução
 */
async function salvarEvolucao() {
    try {
        const descricao = document.getElementById('evolucaoText').value.trim();
        const form = document.getElementById('evolucaoForm');

        // Validação básica
        if (!descricao) {
            mostrarErro("Por favor, preencha a descrição da evolução");
            return;
        }

        if (descricao.length < 10) {
            mostrarErro("A descrição precisa ter pelo menos 10 caracteres");
            return;
        }

        // Desabilita o botão durante o processamento
        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';

        const pacienteId = new URLSearchParams(window.location.search).get('pacienteId');
        const requestBody = {
            idPaciente: pacienteId,
            idFisioterapeuta: usuarioLogado.usuarioId,
            descricao: descricao
        };

        const response = await fetch('/evolucoes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao salvar evolução');
        }

        // Feedback de sucesso
        form.reset();
        mostrarMensagem("Evolução salva com sucesso!", "success");
        await carregarEvolucoes(pacienteId);

    } catch (error) {
        console.error("Erro ao salvar evolução:", error);
        mostrarErro(error.message);
    } finally {
        // Reativa o botão
        const submitBtn = document.querySelector('#evolucaoForm button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-save"></i> Salvar Evolução';
        }
    }
}

/**
 * Salva alterações nos dados do paciente
 * @param {number} pacienteId - ID do paciente
 */
async function salvarAlteracoes(pacienteId) {
    try {
        const nome = document.getElementById('nomeInput').value.trim();
        const dataNascimento = document.getElementById('nascimentoInput').value;

        // Validações
        if (!nome) {
            mostrarErro("O nome não pode estar vazio");
            return;
        }

        if (!dataNascimento) {
            mostrarErro("Data de nascimento inválida");
            return;
        }

        const dadosParaAtualizar = {
            nome: nome,
            dataNascimento: dataNascimento,
            contatos: pacienteAtual.contatos
        };

        const response = await fetch(`/pacientes/${pacienteId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dadosParaAtualizar)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao atualizar paciente');
        }

        // Atualiza localmente e na interface
        pacienteAtual = await response.json();
        preencherDadosPaciente(pacienteAtual);
        toggleEditMode(false);
        mostrarMensagem("Dados atualizados com sucesso!", "success");

    } catch (error) {
        console.error("Erro ao salvar alterações:", error);
        mostrarErro(error.message);
    }
}

/**
 * Inativa um paciente
 * @param {number} id - ID do paciente
 */
async function inativarPaciente(id) {
    try {
        if (!confirm("Tem certeza que deseja inativar este paciente?")) return;

        const response = await fetch(`/pacientes/${id}/inativar`, {
            method: 'PATCH'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao inativar paciente');
        }

        // Atualiza localmente e na interface
        pacienteAtual.ativo = false;
        preencherDadosPaciente(pacienteAtual);
        mostrarMensagem("Paciente inativado com sucesso", "success");

    } catch (error) {
        console.error("Erro ao inativar paciente:", error);
        mostrarErro(error.message);
    }
}

/**
 * Reativa um paciente
 * @param {number} id - ID do paciente
 */
async function ativarPaciente(id) {
    try {
        if (!confirm("Tem certeza que deseja reativar este paciente?")) return;

        const response = await fetch(`/pacientes/${id}/ativar`, {
            method: 'PATCH'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao reativar paciente');
        }

        // Atualiza localmente e na interface
        pacienteAtual.ativo = true;
        preencherDadosPaciente(pacienteAtual);
        mostrarMensagem("Paciente reativado com sucesso", "success");

    } catch (error) {
        console.error("Erro ao reativar paciente:", error);
        mostrarErro(error.message);
    }
}

// =======================================================
// FUNÇÕES AUXILIARES
// =======================================================

/**
 * Formata uma data para o padrão brasileiro
 * @param {string} dateString - Data em formato ISO
 * @returns {string} Data formatada
 */
function formatarData(dateString) {
    try {
        const options = { day: '2-digit', month: '2-digit', year: 'numeric' };
        return new Date(dateString).toLocaleDateString('pt-BR', options);
    } catch {
        return 'Data inválida';
    }
}

/**
 * Renderiza o status do paciente
 * @param {boolean} ativo - Status do paciente
 * @returns {string} HTML do status
 */
function renderizarStatus(ativo) {
    return ativo
        ? '<span class="badge bg-success">Ativo</span>'
        : '<span class="badge bg-danger">Inativo</span>';
}

/**
 * Mostra uma mensagem de feedback
 * @param {string} texto - Texto da mensagem
 * @param {string} tipo - Tipo (success, error, warning)
 */
function mostrarMensagem(texto, tipo = 'success') {
    // Implementação simplificada - pode ser substituída por um toast
    alert(texto);
}