/**
 * @file Lógica da página de Agenda (Compatível com SB Admin 2 e Bootstrap 4)
 * @version 4.0
 */

// As variáveis globais e a maior parte da lógica permanecem as mesmas
let todosPacientes = [];
let todasEspecialidades = [];
let agendamentoInfo = {};

document.addEventListener('DOMContentLoaded', async () => {
    // Código de inicialização (sem alterações)
    const dadosUsuario = JSON.parse(localStorage.getItem('usuarioLogado'));
    if (!dadosUsuario) { alert('Sessão inválida.'); window.location.href = '../../login.html'; return; }
    renderizarComponentesBasicos(dadosUsuario);
    await carregarDadosIniciais();
    if (dadosUsuario.tipoUsuario.includes('FISIOTERAPEUTA')) {
        const fisioSelect = document.getElementById('fisioterapeutaSelect');
        fisioSelect.value = dadosUsuario.usuarioId;
        fisioSelect.disabled = true;
    }
    document.getElementById('dataSelect').valueAsDate = new Date();
    document.getElementById('buscarAgendaBtn').addEventListener('click', renderizarSlotsDoDia);
    document.getElementById('confirmarAgendamentoBtn').addEventListener('click', confirmarAgendamento);
});

// Substitua esta função em meu codigo/pages/agenda/agenda.js
async function renderizarSlotsDoDia() {
    // A lógica de busca inicial continua a mesma
    const fisioId = document.getElementById('fisioterapeutaSelect').value;
    const dataSelecionada = document.getElementById('dataSelect').value;
    const container = document.getElementById('agenda-container');
    const loadingEl = document.getElementById('loading');

    container.innerHTML = '';
    if (!fisioId) {
        alert('Por favor, selecione um fisioterapeuta.');
        return;
    }
    loadingEl.classList.remove('d-none');

    try {
        const [horariosRes, agendamentosRes] = await Promise.all([
            fetch(`/horarios-disponiveis?idFisioterapeuta=${fisioId}&start=${dataSelecionada}&end=${dataSelecionada}`),
            fetch(`/agendamentos?idFisioterapeuta=${fisioId}&inicio=${dataSelecionada}T00:00:00&fim=${dataSelecionada}T23:59:59`)
        ]);

        if (!horariosRes.ok || !agendamentosRes.ok) throw new Error('Falha ao buscar dados da agenda.');

        const horarios = await horariosRes.json();
        const agendamentos = await agendamentosRes.json();

        // ## NOVA LÓGICA DE RENDERIZAÇÃO ##
        // 1. Criamos um mapa apenas com os horários de trabalho como base.
        const todosOsHorariosMap = new Map();
        horarios.forEach(h => todosOsHorariosMap.set(h.horaInicio, h));

        // 2. Criamos um mapa para os agendamentos, para fácil consulta.
        const agendamentosMap = new Map();
        agendamentos.forEach(a => {
            const horaInicio = new Date(a.inicio).toTimeString().substring(0, 8);
            agendamentosMap.set(horaInicio, a);
        });

        if (todosOsHorariosMap.size === 0 && agendamentosMap.size === 0) {
            container.innerHTML = '<div class="col-12"><div class="alert alert-info">Nenhum horário de trabalho configurado para este dia.</div></div>';
            return;
        }

        // 3. Ordenamos as chaves de horário para garantir a ordem na tela.
        const horariosOrdenados = Array.from(todosOsHorariosMap.keys()).sort();
        container.innerHTML = '';

        // 4. Iteramos sobre cada horário de trabalho cadastrado para o dia.
        horariosOrdenados.forEach(horaKey => {
            const agendamento = agendamentosMap.get(horaKey);
            const horario = todosOsHorariosMap.get(horaKey);

            // Se existe um agendamento para este horário...
            if (agendamento) {
                // Se o agendamento foi cancelado, ele é exibido, mas o horário por trás dele fica disponível.
                // Renderizamos o slot do agendamento cancelado PRIMEIRO.
                if (agendamento.status === 'CANCELADO') {
                    container.appendChild(criarSlotOcupado(agendamento));
                }

                // Se o horário está disponível (foi liberado por um cancelamento), renderizamos o slot disponível.
                if (horario.disponivel) {
                    container.appendChild(criarSlotDisponivel(horario));
                }
                // Se não foi cancelado e não está disponível, renderizamos apenas o slot ocupado.
                else if (agendamento.status !== 'CANCELADO' && !horario.disponivel) {
                    container.appendChild(criarSlotOcupado(agendamento));
                }

            } else if (horario.disponivel) {
                // Se não há agendamento e o horário está disponível, apenas renderizamos o slot disponível.
                container.appendChild(criarSlotDisponivel(horario));
            }
        });

    } catch (error) {
        container.innerHTML = `<div class="col-12"><div class="alert alert-danger">${error.message}</div></div>`;
    } finally {
        loadingEl.classList.add('d-none');
    }
}

/**
 * Nova função auxiliar para criar o HTML de um slot disponível.
 * @param {object} h - O objeto do horário disponível.
 * @returns {HTMLElement} - O elemento div do wrapper.
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
 * Nova função auxiliar para criar o HTML de um slot ocupado.
 * @param {object} a - O objeto do agendamento.
 * @returns {HTMLElement} - O elemento div do wrapper.
 */
function criarSlotOcupado(a) {
    const wrapper = document.createElement('div');
    wrapper.className = 'col-lg-4 col-md-6 mb-3';
    const hora = new Date(a.inicio).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    const statusClass = `status-${a.status.toLowerCase().replace('_', '-')}`;
    const statusText = a.status.replace(/_/g, ' ');

    const acoesDropdownHTML = a.status === 'CONFIRMADO' ? `
        <div class="dropdown">
            <button class="btn btn-sm btn-light dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
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
                <div class="ml-2">
                    ${acoesDropdownHTML}
                </div>
            </div>
        </div>`;
    return wrapper;
}

// O restante do seu script não precisa de alterações.
// As funções abaixo já estão corretas.

function abrirModalAgendamento(data, horaInicio, horaFim) {
    agendamentoInfo = { horaInicio, horaFim, idFisioterapeuta: document.getElementById('fisioterapeutaSelect').value };
    const start = new Date(`${data}T${horaInicio}`);
    const fisioSelect = document.getElementById('fisioterapeutaSelect');
    document.getElementById('modalFisioNome').textContent = fisioSelect.options[fisioSelect.selectedIndex].text;
    document.getElementById('modalHorario').textContent = start.toLocaleString('pt-BR', { dateStyle: 'full', timeStyle: 'short' });
    const pacienteSelect = document.getElementById('pacienteSelect');
    pacienteSelect.innerHTML = '<option value="">Selecione...</option>';
    todosPacientes.forEach(p => pacienteSelect.add(new Option(p.nome, p.id)));
    const especialidadeSelect = document.getElementById('especialidadeSelect');
    especialidadeSelect.innerHTML = '<option value="">Selecione...</option>';
    todasEspecialidades.forEach(e => especialidadeSelect.add(new Option(e.nome, e.idEspecialidade)));
    document.getElementById('resultadoModal').innerHTML = '';
    $('#agendamentoModal').modal('show');
}
function abrirModalDetalhes(paciente, status) {
    document.getElementById('detalhe-paciente').textContent = paciente;
    document.getElementById('detalhe-status').textContent = status;
    $('#detalhesConsultaModal').modal('show');
}
async function confirmarAgendamento() {
    const dataSelecionada = document.getElementById('dataSelect').value;
    const requestBody = { idPaciente: document.getElementById('pacienteSelect').value, idFisioterapeuta: agendamentoInfo.idFisioterapeuta, idEspecialidade: document.getElementById('especialidadeSelect').value, dataHoraInicio: `${dataSelecionada}T${agendamentoInfo.horaInicio}`, dataHoraFim: `${dataSelecionada}T${agendamentoInfo.horaFim}` };
    try {
        if (!requestBody.idPaciente || !requestBody.idEspecialidade) throw new Error('Selecione paciente e especialidade.');
        const response = await fetch('/agendamentos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(requestBody) });
        if (!response.ok) throw new Error((await response.json()).message || 'Falha ao agendar.');
        $('#agendamentoModal').modal('hide');
        renderizarSlotsDoDia();
    } catch (error) {
        document.getElementById('resultadoModal').innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
    }
}
async function atualizarStatus(agendamentoId, novoStatus) {
    if (!confirm(`Tem certeza que deseja marcar esta consulta como "${novoStatus.replace('_', ' ')}"?`)) return;
    try {
        const response = await fetch(`/agendamentos/${agendamentoId}/status`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ novoStatus: novoStatus.toUpperCase() }) });
        if (!response.ok) throw new Error('Falha ao atualizar o status.');
        renderizarSlotsDoDia();
    } catch (error) {
        alert(error.message);
    }
}
async function carregarDadosIniciais() { await Promise.all([carregarFisioterapeutas(), carregarPacientes(), carregarEspecialidades()]); }
async function carregarFisioterapeutas() { try { const r = await fetch('/funcionarios'); const d = await r.json(); const s = document.getElementById('fisioterapeutaSelect'); s.innerHTML = '<option value="">Selecione...</option>'; d.filter(f => f.tipo === 'FISIOTERAPEUTA').forEach(f => s.add(new Option(f.nome, f.id))); } catch (e) { console.error('Erro ao carregar Fisioterapeutas:', e); } }
async function carregarPacientes() { try { const r = await fetch('/pacientes'); todosPacientes = await r.json(); } catch (e) { console.error('Erro ao carregar Pacientes:', e); } }
async function carregarEspecialidades() { try { const r = await fetch('/especialidades'); todasEspecialidades = await r.json(); } catch (e) { console.error('Erro ao carregar Especialidades:', e); } }
function renderizarComponentesBasicos(dadosUsuario) {
    document.getElementById('userName').textContent = dadosUsuario.nome;
    renderizarSidebar(dadosUsuario.tipoUsuario);
}
function renderizarSidebar(t) {
    const c = document.getElementById('sidebar-links');
    if (!c) return;
    c.innerHTML = '';
    if (t.includes('ADMIN')) { c.innerHTML += `<li class="nav-item"><a class="nav-link" href="../funcionarios/funcionarios.html"><i class="fas fa-fw fa-users-cog"></i><span>Gerenciar Equipe</span></a></li>`; c.innerHTML += `<li class="nav-item"><a class="nav-link" href="../relatorios/relatorios.html"><i class="fas fa-fw fa-chart-bar"></i><span>Relatórios</span></a></li>`; }
    if (t.includes('FISIOTERAPEUTA')) { c.innerHTML += `<li class="nav-item"><a class="nav-link" href="../meus-horarios/meus-horarios.html"><i class="fas fa-fw fa-clock"></i><span>Meus Horários</span></a></li>`; }
    c.innerHTML += `<li class="nav-item active"><a class="nav-link" href="agenda.html"><i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span></a></li>`;
    document.getElementById('logoutButton').addEventListener('click', () => { localStorage.clear(); window.location.href = '../../login.html'; });
}