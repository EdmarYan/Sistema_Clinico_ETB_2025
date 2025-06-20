/**
 * @file Lógica da página de Agenda (Compatível com SB Admin 2 e Bootstrap 4)
 * @description Este script gerencia toda a interatividade da página da agenda, incluindo
 * a busca e renderização de horários, a criação de novos agendamentos via modal
 * e a atualização de status das consultas.
 * @version 4.0
 */

// ===============================================================
// VARIÁVEIS GLOBAIS
// Armazenam dados carregados da API para evitar múltiplas requisições.
// ===============================================================
let todosPacientes = [];
let todasEspecialidades = [];
// Guarda informações do slot de horário selecionado para usar no modal.
let agendamentoInfo = {};

// ===============================================================
// INICIALIZAÇÃO DA PÁGINA
// ===============================================================
// Garante que o script só execute após o carregamento completo do HTML.
document.addEventListener('DOMContentLoaded', async () => {
    // 1. Validação de sessão.
    const dadosUsuario = JSON.parse(localStorage.getItem('usuarioLogado'));
    if (!dadosUsuario) {
        alert('Sessão inválida. Por favor, faça o login novamente.');
        window.location.href = '../../login.html';
        return;
    }

    // 2. Renderização de componentes básicos da página (menu, nome do usuário).
    renderizarComponentesBasicos(dadosUsuario);

    // 3. Carregamento dos dados iniciais necessários para os filtros.
    await carregarDadosIniciais();

    // 4. Lógica de perfil: Se o usuário for FISIOTERAPEUTA, pré-seleciona seu nome e desabilita o filtro.
    if (dadosUsuario.tipoUsuario.includes('FISIOTERAPEUTA')) {
        const fisioSelect = document.getElementById('fisioterapeutaSelect');
        fisioSelect.value = dadosUsuario.usuarioId;
        fisioSelect.disabled = true;
    }

    // 5. Define a data de hoje como padrão no seletor de data.
    document.getElementById('dataSelect').valueAsDate = new Date();

    // 6. Adiciona os listeners de eventos aos botões principais.
    document.getElementById('buscarAgendaBtn').addEventListener('click', renderizarSlotsDoDia);
    document.getElementById('confirmarAgendamentoBtn').addEventListener('click', confirmarAgendamento);
});


// ===============================================================
// RENDERIZAÇÃO DA AGENDA
// ===============================================================

/**
 * Função principal que busca e renderiza os slots de horários para o dia selecionado.
 * Esta é a função mais complexa da página, responsável por:
 * - Buscar os horários de trabalho e os agendamentos existentes.
 * - Combinar essas duas informações para montar a visualização da agenda.
 * - Lidar com o caso especial de horários cancelados que voltam a ficar disponíveis.
 */
async function renderizarSlotsDoDia() {
    // Seleciona os elementos do DOM necessários.
    const fisioId = document.getElementById('fisioterapeutaSelect').value;
    const dataSelecionada = document.getElementById('dataSelect').value;
    const container = document.getElementById('agenda-container');
    const loadingEl = document.getElementById('loading');

    // Limpa a agenda e valida se um fisioterapeuta foi selecionado.
    container.innerHTML = '';
    if (!fisioId) {
        alert('Por favor, selecione um fisioterapeuta.');
        return;
    }
    loadingEl.classList.remove('d-none'); // Mostra o spinner de carregamento.

    try {
        // Otimização: Dispara as duas requisições para a API em paralelo usando Promise.all.
        const [horariosRes, agendamentosRes] = await Promise.all([
            fetch(`/horarios-disponiveis?idFisioterapeuta=${fisioId}&start=${dataSelecionada}&end=${dataSelecionada}`),
            fetch(`/agendamentos?idFisioterapeuta=${fisioId}&inicio=${dataSelecionada}T00:00:00&fim=${dataSelecionada}T23:59:59`)
        ]);

        if (!horariosRes.ok || !agendamentosRes.ok) throw new Error('Falha ao buscar dados da agenda.');

        const horarios = await horariosRes.json();
        const agendamentos = await agendamentosRes.json();

        // Estruturas de Mapa para busca rápida, melhorando a performance.
        const todosOsHorariosMap = new Map();
        horarios.forEach(h => todosOsHorariosMap.set(h.horaInicio, h));

        const agendamentosMap = new Map();
        agendamentos.forEach(a => {
            const horaInicio = new Date(a.inicio).toTimeString().substring(0, 8);
            agendamentosMap.set(horaInicio, a);
        });

        // Caso não haja horários configurados para o dia.
        if (todosOsHorariosMap.size === 0 && agendamentosMap.size === 0) {
            container.innerHTML = '<div class="col-12"><div class="alert alert-info">Nenhum horário de trabalho configurado para este dia.</div></div>';
            return;
        }

        // Ordena os horários para garantir a renderização na ordem correta.
        const horariosOrdenados = Array.from(todosOsHorariosMap.keys()).sort();
        container.innerHTML = '';

        // Itera sobre cada horário de trabalho cadastrado para o dia.
        horariosOrdenados.forEach(horaKey => {
            const agendamento = agendamentosMap.get(horaKey);
            const horario = todosOsHorariosMap.get(horaKey);

            if (agendamento) {
                // Regra de Negócio: Se um agendamento foi cancelado, o sistema deve mostrar
                // tanto o card de "cancelado" (para histórico) quanto o slot de "disponível"
                // para um novo agendamento.
                if (agendamento.status === 'CANCELADO') {
                    container.appendChild(criarSlotOcupado(agendamento));
                }
                if (horario.disponivel) {
                    container.appendChild(criarSlotDisponivel(horario));
                } else if (agendamento.status !== 'CANCELADO') {
                    container.appendChild(criarSlotOcupado(agendamento));
                }
            } else if (horario.disponivel) {
                // Se não há agendamento e o horário está disponível.
                container.appendChild(criarSlotDisponivel(horario));
            }
        });

    } catch (error) {
        // Tratamento de erro para falhas na API.
        container.innerHTML = `<div class="col-12"><div class="alert alert-danger">${error.message}</div></div>`;
    } finally {
        loadingEl.classList.add('d-none'); // Esconde o spinner de carregamento.
    }
}

/**
 * Função auxiliar para criar o HTML de um slot disponível.
 * @param {object} h - O objeto do horário disponível.
 * @returns {HTMLElement} - O elemento div do slot.
 */
function criarSlotDisponivel(h) {
    const wrapper = document.createElement('div');
    wrapper.className = 'col-lg-4 col-md-6 mb-3';
    wrapper.innerHTML = `
        <div class="slot slot-disponivel h-100 d-flex flex-column justify-content-center text-center p-3" 
             onclick="abrirModalAgendamento('${h.data}', '${h.horaInicio}', '${h.horaFim}')">
            <strong class="fs-5">${h.horaInicio.substring(0, 5)}</strong>
            <span class="mt-1">Disponível</span>
        </div>`;
    return wrapper;
}

/**
 * Função auxiliar para criar o HTML de um slot ocupado.
 * @param {object} a - O objeto do agendamento.
 * @returns {HTMLElement} - O elemento div do slot.
 */
function criarSlotOcupado(a) {
    const wrapper = document.createElement('div');
    wrapper.className = 'col-lg-4 col-md-6 mb-3';
    const hora = new Date(a.inicio).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    const statusClass = `status-${a.status.toLowerCase().replace('_', '-')}`;
    const statusText = a.status.replace(/_/g, ' ');

    // Renderiza o menu de ações apenas se o status for 'CONFIRMADO'.
    const acoesDropdownHTML = a.status === 'CONFIRMADO' ? `
        <div class="dropdown">
            <button class="btn btn-sm btn-light dropdown-toggle" type="button" data-toggle="dropdown">
                Atualizar Status
            </button>
            <div class="dropdown-menu dropdown-menu-right">
                <a class="dropdown-item" href="#" onclick="atualizarStatus(${a.id}, 'REALIZADO')"><i class="fas fa-check-circle text-success fa-fw mr-2"></i>Realizado</a>
                <a class="dropdown-item" href="#" onclick="atualizarStatus(${a.id}, 'NAO_COMPARECEU')"><i class="fas fa-user-times text-warning fa-fw mr-2"></i>Não Compareceu</a>
                <div class="dropdown-divider"></div>
                <a class="dropdown-item text-danger" href="#" onclick="atualizarStatus(${a.id}, 'CANCELADO')"><i class="fas fa-times-circle text-danger fa-fw mr-2"></i>Cancelar</a>
            </div>
        </div>` : '';

    wrapper.innerHTML = `
        <div class="slot slot-ocupado ${statusClass} p-3 h-100">
            <div class="d-flex justify-content-between align-items-center h-100">
                <div class="flex-grow-1" onclick="abrirModalDetalhes('${a.nomePaciente}', '${statusText}')" style="cursor: pointer;">
                    <strong class="fs-5">${hora}</strong>
                    <div class="font-weight-bold">${a.nomePaciente}</div>
                    <span class="badge badge-secondary text-uppercase">${statusText}</span>
                </div>
                <div class="ml-2">${acoesDropdownHTML}</div>
            </div>
        </div>`;
    return wrapper;
}

// ===============================================================
// FUNÇÕES DE INTERAÇÃO (MODAIS E ATUALIZAÇÕES)
// ===============================================================

/** Abre o modal de novo agendamento, preenchendo os dados do horário selecionado. */
function abrirModalAgendamento(data, horaInicio, horaFim) {
    // Validação de horário passado - não permite abrir o modal para slots antigos
    const now = new Date();
    const slotTime = new Date(`${data}T${horaInicio}`);

    // Bloqueia horários passados com feedback visual
    if (slotTime < now) {
        alert('Este horário já passou. Por favor, selecione um horário futuro.');
        return;
    }

    // Armazena informações do slot selecionado
    agendamentoInfo = {
        horaInicio,
        horaFim,
        idFisioterapeuta: document.getElementById('fisioterapeutaSelect').value
    };

    // Formata e exibe informações no modal
    const start = new Date(`${data}T${horaInicio}`);
    const fisioSelect = document.getElementById('fisioterapeutaSelect');
    document.getElementById('modalFisioNome').textContent = fisioSelect.options[fisioSelect.selectedIndex].text;
    document.getElementById('modalHorario').textContent = start.toLocaleString('pt-BR', {
        dateStyle: 'full',
        timeStyle: 'short'
    });

    // Popula dropdown de pacientes
    const pacienteSelect = document.getElementById('pacienteSelect');
    pacienteSelect.innerHTML = '<option value="">Selecione o paciente...</option>';
    todosPacientes.forEach(p => {
        pacienteSelect.add(new Option(p.nome, p.id));
    });

    // Popula dropdown de especialidades
    const especialidadeSelect = document.getElementById('especialidadeSelect');
    especialidadeSelect.innerHTML = '<option value="">Selecione a especialidade...</option>';
    todasEspecialidades.forEach(e => {
        especialidadeSelect.add(new Option(e.nome, e.idEspecialidade));
    });

    // Limpa mensagens de erro anteriores
    document.getElementById('resultadoModal').innerHTML = '';

    // Exibe o modal
    $('#agendamentoModal').modal('show');
}

/** Abre o modal para exibir os detalhes de um agendamento existente. */
function abrirModalDetalhes(paciente, status) {
    document.getElementById('detalhe-paciente').textContent = paciente;
    document.getElementById('detalhe-status').textContent = status;
    $('#detalhesConsultaModal').modal('show');
}

/** Confirma e envia a requisição para criar um novo agendamento. */
async function confirmarAgendamento() {
    const dataSelecionada = document.getElementById('dataSelect').value;
    const requestBody = {
        idPaciente: document.getElementById('pacienteSelect').value,
        idFisioterapeuta: agendamentoInfo.idFisioterapeuta,
        idEspecialidade: document.getElementById('especialidadeSelect').value,
        dataHoraInicio: `${dataSelecionada}T${agendamentoInfo.horaInicio}`,
        dataHoraFim: `${dataSelecionada}T${agendamentoInfo.horaFim}`
    };

    // Limpa mensagens anteriores
    document.getElementById('resultadoModal').innerHTML = '';

    // Validação preventiva
    const now = new Date();
    const start = new Date(requestBody.dataHoraInicio);
    const end = new Date(requestBody.dataHoraFim);

    if (start < now || end < now) {
        document.getElementById('resultadoModal').innerHTML = `
            <div class="alert alert-danger">
                Não é possível agendar para horários passados
            </div>`;
        return;
    }

    try {
        const response = await fetch('/agendamentos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            const errorData = await response.json();

            // Tratamento de erros de validação
            if (errorData.errors) {
                const errorHtml = `
                    <div class="alert alert-danger">
                        <strong>Erro no agendamento:</strong>
                        <ul class="mb-0">
                            ${errorData.errors.map(e => `<li>${e}</li>`).join('')}
                        </ul>
                    </div>`;
                document.getElementById('resultadoModal').innerHTML = errorHtml;
                return;
            }

            // Tratamento de outros tipos de erro
            throw new Error(errorData.message || 'Falha ao agendar');
        }

        $('#agendamentoModal').modal('hide');
        renderizarSlotsDoDia();
    } catch (error) {
        document.getElementById('resultadoModal').innerHTML = `
            <div class="alert alert-danger">
                ${error.message}
            </div>`;
    }
}

/** Envia a requisição para atualizar o status de um agendamento. */
async function atualizarStatus(agendamentoId, novoStatus) {
    if (!confirm(`Tem certeza que deseja marcar esta consulta como "${novoStatus.replace('_', ' ')}"?`)) return;
    try {
        const response = await fetch(`/agendamentos/${agendamentoId}/status`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ novoStatus: novoStatus.toUpperCase() }) });
        if (!response.ok) throw new Error('Falha ao atualizar o status.');
        renderizarSlotsDoDia(); // Atualiza a agenda para refletir a mudança de status.
    } catch (error) {
        alert(error.message);
    }
}

// ===============================================================
// FUNÇÕES DE CARREGAMENTO DE DADOS INICIAIS
// ===============================================================
async function carregarDadosIniciais() { await Promise.all([carregarFisioterapeutas(), carregarPacientes(), carregarEspecialidades()]); }
async function carregarFisioterapeutas() { try { const r = await fetch('/funcionarios'); const d = await r.json(); const s = document.getElementById('fisioterapeutaSelect'); s.innerHTML = '<option value="">Selecione...</option>'; d.filter(f => f.tipo === 'FISIOTERAPEUTA' && f.ativo).forEach(f => s.add(new Option(f.nome, f.id))); } catch (e) { console.error('Erro ao carregar Fisioterapeutas:', e); } }
async function carregarPacientes() { try { const r = await fetch('/pacientes'); todosPacientes = await r.json(); } catch (e) { console.error('Erro ao carregar Pacientes:', e); } }
async function carregarEspecialidades() { try { const r = await fetch('/especialidades'); todasEspecialidades = await r.json(); } catch (e) { console.error('Erro ao carregar Especialidades:', e); } }

// ===============================================================
// FUNÇÕES DE RENDERIZAÇÃO DA INTERFACE BÁSICA (SIDEBAR, ETC.)
// ===============================================================
function renderizarComponentesBasicos(dadosUsuario) {
    document.getElementById('userName').textContent = dadosUsuario.nome;
    renderizarSidebar(dadosUsuario.tipoUsuario);
    document.getElementById('logoutButton').addEventListener('click', () => { localStorage.clear(); window.location.href = '../../login.html'; });
}
function renderizarSidebar(t) {
    const c = document.getElementById('sidebar-links');
    if (!c) return;
    c.innerHTML = '';
    if (t.includes('ADMIN')) { c.innerHTML += `<li class="nav-item"><a class="nav-link" href="../funcionarios/funcionarios.html"><i class="fas fa-fw fa-users-cog"></i><span>Gerenciar Equipe</span></a></li>`; c.innerHTML += `<li class="nav-item"><a class="nav-link" href="../relatorios/relatorios.html"><i class="fas fa-fw fa-chart-bar"></i><span>Relatórios</span></a></li>`; }
    if (t.includes('FISIOTERAPEUTA')) { c.innerHTML += `<li class="nav-item"><a class="nav-link" href="../meus-horarios/meus-horarios.html"><i class="fas fa-fw fa-clock"></i><span>Meus Horários</span></a></li>`; }
    c.innerHTML += `<li class="nav-item active"><a class="nav-link" href="agenda.html"><i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span></a></li>`;
}
