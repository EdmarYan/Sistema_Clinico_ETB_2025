/**
 * @file Lógica da página 'Meus Horários', para Fisioterapeutas.
 * @description Gerencia a grade de trabalho semanal, incluindo turnos pré-definidos,
 * e a geração da agenda mensal, utilizando um modal customizado para confirmações.
 */

// Variável global para armazenar os dados do usuário logado.
let usuarioLogado;

/**
 * Ponto de entrada do script. Roda quando o HTML da página é carregado.
 * Configura a proteção de rota e todos os listeners de eventos da página.
 */
document.addEventListener('DOMContentLoaded', function() {
    // 1. Validação de Sessão
    usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado'));
    if (!usuarioLogado || !usuarioLogado.tipoUsuario.includes('FISIOTERAPEUTA')) {
        alert('Acesso negado. Esta página é apenas para Fisioterapeutas.'); // Alert mantido para bloqueio imediato.
        window.location.href = '../../pages/dashboard/dashboard.html';
        return;
    }

    // 2. Inicialização da Interface
    document.getElementById('userName').textContent = usuarioLogado.nome;
    document.getElementById('welcomeHeader').textContent = `Meus Horários de Trabalho - ${usuarioLogado.nome}`;
    renderizarSidebar(usuarioLogado.tipoUsuario);
    carregarHorarios();
    preencherDataAtual();

    // 3. Listeners de Eventos
    document.getElementById('formNovoHorario').addEventListener('submit', adicionarHorario);
    document.getElementById('gerarAgendaBtn').addEventListener('click', gerarAgenda);
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '../../login.html';
    });
    document.getElementById('turnoManhaBtn').addEventListener('click', () => preencherTurno('08:00', '12:00'));
    document.getElementById('turnoTardeBtn').addEventListener('click', () => preencherTurno('13:00', '18:00'));
    document.getElementById('turnoIntegralBtn').addEventListener('click', () => preencherTurno('08:00', '18:00'));
});

/**
 * Preenche o input de mês/ano com o valor do mês e ano atuais.
 */
function preencherDataAtual() {
    const hoje = new Date();
    const mes = (hoje.getMonth() + 1).toString().padStart(2, '0');
    const ano = hoje.getFullYear();
    document.getElementById('mesAnoInput').value = `${ano}-${mes}`;
}

/**
 * Preenche os campos de hora com base no botão de turno clicado.
 * @param {string} inicio - A hora de início do turno (ex: "08:00").
 * @param {string} fim - A hora de fim do turno (ex: "12:00").
 */
function preencherTurno(inicio, fim) {
    document.getElementById('horaInicio').value = inicio;
    document.getElementById('horaFim').value = fim;
}

/**
 * Renderiza os links do menu lateral, marcando 'Meus Horários' como a página ativa.
 */
function renderizarSidebar(tipoUsuario) {
    const sidebarContainer = document.getElementById('sidebar-links');
    if (!sidebarContainer) return;
    sidebarContainer.innerHTML = '';
    if (tipoUsuario.includes('ADMIN')) {
        // Links de Admin poderiam ser adicionados aqui se necessário.
    }
    sidebarContainer.innerHTML += `<li class="nav-item active"><a class="nav-link" href="meus-horarios.html"><i class="fas fa-fw fa-clock"></i><span>Meus Horários</span></a></li>`;
    sidebarContainer.innerHTML += `<li class="nav-item"><a class="nav-link" href="../agenda/agenda.html"><i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span></a></li>`;
}

/**
 * Cria um elemento <li> da lista para um horário de trabalho.
 * @param {object} horario - O objeto de horário vindo da API.
 * @returns {HTMLLIElement} O elemento <li> pronto para ser inserido no DOM.
 */
function criarElementoHorario(horario) {
    const li = document.createElement('li');
    li.className = 'list-group-item d-flex justify-content-between align-items-center';
    li.id = `horario-${horario.id}`;
    li.innerHTML = `
        <span><strong>${horario.nomeDiaSemana}:</strong> das ${horario.horaInicio.substring(0,5)} às ${horario.horaFim.substring(0,5)}</span>
        <button class="btn btn-sm btn-outline-danger" onclick="removerHorario(${horario.id})">&times;</button>
    `;
    return li;
}

/**
 * Busca e exibe a grade de trabalho semanal do fisioterapeuta logado na interface.
 */
async function carregarHorarios() {
    const listaEl = document.getElementById('listaHorarios');
    const loadingEl = document.getElementById('loading');
    loadingEl.style.display = 'block';
    listaEl.innerHTML = '';
    try {
        const response = await fetch(`/horarios-trabalho/fisioterapeuta/${usuarioLogado.usuarioId}`);
        if (!response.ok) throw new Error('Falha ao carregar horários.');
        const horarios = await response.json();
        if (horarios.length === 0) {
            listaEl.innerHTML = '<li class="list-group-item text-muted" id="horario-placeholder">Nenhum horário de trabalho definido.</li>';
        } else {
            horarios.sort((a, b) => a.diaDaSemana.localeCompare(b.diaDaSemana) || a.horaInicio.localeCompare(b.horaInicio));
            horarios.forEach(h => listaEl.appendChild(criarElementoHorario(h)));
        }
    } catch (error) {
        listaEl.innerHTML = `<li class="list-group-item text-danger">${error.message}</li>`;
    } finally {
        loadingEl.style.display = 'none';
    }
}

/**
 * Adiciona um novo horário na grade semanal após submissão do formulário.
 * @param {Event} event - O evento de submissão do formulário.
 */
async function adicionarHorario(event) {
    event.preventDefault();
    const resultadoDiv = document.getElementById('resultadoAdicao');
    const requestBody = {
        idFisioterapeuta: usuarioLogado.usuarioId,
        diaDaSemana: document.getElementById('diaDaSemana').value,
        horaInicio: document.getElementById('horaInicio').value,
        horaFim: document.getElementById('horaFim').value,
    };
    if (!requestBody.diaDaSemana) {
        showConfirmationModal('Por favor, selecione um dia da semana.', () => {});
        return;
    }
    resultadoDiv.className = '';
    resultadoDiv.textContent = '';
    try {
        const response = await fetch('/horarios-trabalho', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });
        if(!response.ok) {
            const erro = await response.json();
            throw new Error(erro.message || 'Não foi possível adicionar o horário.');
        }
        await carregarHorarios();
        document.getElementById('formNovoHorario').reset();
        resultadoDiv.className = 'alert alert-success mt-3';
        resultadoDiv.textContent = 'Horário adicionado com sucesso!';
        setTimeout(() => resultadoDiv.innerHTML = '', 3000);
    } catch (error) {
        resultadoDiv.className = 'alert alert-danger mt-3';
        resultadoDiv.textContent = error.message;
    }
}

/**
 * Pede confirmação via modal e, se confirmado, remove um horário da grade.
 * @param {number} id - O ID do horário a ser removido.
 */
function removerHorario(id) {
    showConfirmationModal('Tem certeza que deseja remover este horário da sua grade?', async () => {
        try {
            const response = await fetch(`/horarios-trabalho/${id}`, { method: 'DELETE' });
            if (!response.ok) throw new Error('Falha ao remover o horário.');
            document.getElementById(`horario-${id}`).remove();
            const listaEl = document.getElementById('listaHorarios');
            if (listaEl.children.length === 0) {
                listaEl.innerHTML = '<li class="list-group-item text-muted" id="horario-placeholder">Nenhum horário de trabalho definido.</li>';
            }
        } catch (error) {
            showConfirmationModal(error.message, () => {});
        }
    });
}

/**
 * Pede confirmação via modal e, se confirmado, envia o comando para o backend gerar a agenda.
 */
function gerarAgenda() {
    const mesAno = document.getElementById('mesAnoInput').value;
    const [ano, mes] = mesAno.split('-');
    const mesAtual = new Date().toISOString().slice(0, 7); // Formato YYYY-MM

    // Valida se o mês selecionado é futuro ou atual
    if (mesAno < mesAtual) {
        showConfirmationModal(
            '⚠️ Atenção! Não é possível gerar agenda para meses passados. ' +
            'Por favor, selecione um mês futuro ou o mês atual.',
            () => {}
        );
        return;
    }

    const mesFormatado = mesAno.split('-').reverse().join('/'); // Formato MM/YYYY
    showConfirmationModal(
        `Isso irá recriar todos os seus horários disponíveis para ${mesFormatado}. ` +
        'Os horários existentes serão substituídos. Deseja continuar?',
        async () => {
            const resultadoDiv = document.getElementById('resultadoGeracao');
            const btn = document.getElementById('gerarAgendaBtn');

            btn.disabled = true;
            resultadoDiv.innerHTML = '<div class="spinner-border spinner-border-sm" role="status"></div> Gerando agenda...';
            resultadoDiv.className = 'alert alert-info mt-3';

            try {
                const response = await fetch(`/horarios-trabalho/gerar-disponibilidade?idFisioterapeuta=${usuarioLogado.usuarioId}&ano=${ano}&mes=${mes}`, {
                    method: 'POST'
                });

                if (!response.ok) {
                    let errorMessage = 'Falha ao gerar agenda.';
                    try {
                        const erro = await response.json();
                        errorMessage = erro.message || errorMessage;
                    } catch (e) {
                        // Não foi possível parsear o JSON de erro
                    }
                    throw new Error(errorMessage);
                }

                resultadoDiv.className = 'alert alert-success mt-3';
                resultadoDiv.innerHTML = `
                    <i class="fas fa-check-circle mr-2"></i>
                    Agenda gerada com sucesso para ${mesFormatado}!
                    <div class="mt-2 small">Os novos horários já estão disponíveis para agendamento.</div>
                `;
            } catch (error) {
                resultadoDiv.className = 'alert alert-danger mt-3';
                resultadoDiv.innerHTML = `
                    <i class="fas fa-exclamation-triangle mr-2"></i>
                    <strong>Erro:</strong> ${error.message}
                    <div class="mt-2 small">Tente novamente ou contate o suporte.</div>
                `;
            } finally {
                btn.disabled = false;
            }
        }
    );
}

/**
 * Exibe um modal de confirmação genérico.
 * @param {string} message - A mensagem a ser exibida no corpo do modal.
 * @param {Function} onConfirm - A função (callback) a ser executada se o usuário clicar em "Confirmar".
 */
function showConfirmationModal(message, onConfirm) {
    const modal = $('#confirmationModal');
    const modalBody = document.getElementById('confirmationModalBody');
    const confirmBtn = document.getElementById('confirmActionBtn');
    modalBody.textContent = message;
    $(confirmBtn).off('click').on('click', () => {
        modal.modal('hide');
        onConfirm();
    });
    modal.modal('show');
}

