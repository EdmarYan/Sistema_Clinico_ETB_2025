/**
 * Arquivo: dashboard.js
 * Descrição: Contém toda a lógica de frontend para a página de Dashboard.
 * Responsabilidades:
 * - Validação de sessão do usuário.
 * - Renderização dinâmica de componentes baseada no perfil do usuário.
 * - Funcionalidade de busca de pacientes por Nome ou CPF.
 * - Lógica de logout.
 */

// Este evento garante que o script só rode depois que a página HTML inteira for carregada.
document.addEventListener('DOMContentLoaded', function() {

    // --- 1. VERIFICAÇÃO DE SEGURANÇA ---
    // Busca os dados do usuário salvos no localStorage durante o login.
    const dadosUsuario = JSON.parse(localStorage.getItem('usuarioLogado'));

    // Se não houver dados ou o nome do usuário não existir, o usuário não está logado.
    // Ele é redirecionado imediatamente para a tela de login para proteger a página.
    if (!dadosUsuario || !dadosUsuario.nome) {
        alert("Acesso não autorizado. Por favor, faça o login.");
        window.location.href = '../../login.html';
        return; // Interrompe a execução do script.
    }

    // --- 2. INICIALIZAÇÃO DA PÁGINA ---
    // Preenche o nome do usuário na barra de navegação superior.
    document.getElementById('userName').textContent = dadosUsuario.nome;

    // Chama a função para desenhar os componentes corretos para este tipo de usuário.
    renderizarComponentes(dadosUsuario.tipoUsuario);

    // Carrega a lista inicial de pacientes ativos na tabela.
    carregarPacientesIniciais();

    // --- 3. LISTENERS DE EVENTOS ---
    // Adiciona o evento de clique para o botão de logout.
    document.getElementById('logoutButton').addEventListener('click', (e) => {
        e.preventDefault(); // Previne qualquer ação padrão do link.
        localStorage.clear(); // Limpa todos os dados salvos no localStorage.
        window.location.href = '../../login.html'; // Redireciona para o login.
    });

    // Adiciona o evento de submissão para o formulário de busca.
    document.getElementById('formBuscaPaciente').addEventListener('submit', (e) => {
        e.preventDefault(); // Previne o recarregamento da página.
        buscarPacientes(); // Chama a função de busca.
    });
});

/**
 * Função que desenha os componentes visuais (cards de atalho e links do menu)
 * de acordo com o perfil do usuário logado (Controle de Acesso Baseado em Papel - RBAC no frontend).
 * @param {string} tipoUsuario - O tipo do usuário vindo da API (ex: 'ADMIN', 'FISIOTERAPEUTA').
 */
function renderizarComponentes(tipoUsuario) {
    const cardsContainer = document.getElementById('cards-container');
    const sidebarContainer = document.getElementById('sidebar-links');

    // Limpa os contêineres para garantir que não haja conteúdo duplicado.
    cardsContainer.innerHTML = '';
    sidebarContainer.innerHTML = '';

    // Lógica para renderizar os CARDS de atalho com base no perfil.
    if (tipoUsuario.includes('ADMIN') || tipoUsuario.includes('RECEPCIONISTA')) {
        cardsContainer.innerHTML += createCard('Pacientes', 'Novo Cadastro', '../cadastro-paciente/cadastro-paciente.html', 'primary', 'user-plus');
    }
    if (tipoUsuario.includes('ADMIN')) {
        cardsContainer.innerHTML += createCard('Equipe', 'Gerenciar', '../funcionarios/funcionarios.html', 'danger', 'users-cog');
        cardsContainer.innerHTML += createCard('Relatórios', 'Ver Desempenho', '../relatorios/relatorios.html', 'warning', 'chart-line');
    }
    if (tipoUsuario.includes('FISIOTERAPEUTA')) {
        cardsContainer.innerHTML += createCard('Minha Área', 'Definir Horários', '../meus-horarios/meus-horarios.html', 'info', 'id-badge');
    }
    // Card de Agenda é visível para todos os funcionários logados.
    cardsContainer.innerHTML += createCard('Agenda', 'Acessar', '../agenda/agenda.html', 'success', 'calendar-alt');

    // Lógica para renderizar os LINKS do menu lateral com base no perfil.
    if (tipoUsuario.includes('ADMIN')) {
        sidebarContainer.innerHTML += createSidebarLink('Gerenciar Equipe', '../funcionarios/funcionarios.html', 'users-cog');
        sidebarContainer.innerHTML += createSidebarLink('Relatórios', '../relatorios/relatorios.html', 'chart-bar');
    }
    if (tipoUsuario.includes('FISIOTERAPEUTA')) {
        sidebarContainer.innerHTML += createSidebarLink('Meus Horários', '../meus-horarios/meus-horarios.html', 'clock');
    }
    sidebarContainer.innerHTML += createSidebarLink('Agenda', '../agenda/agenda.html', 'calendar-alt');
}

// --- FUNÇÕES AUXILIARES PARA RENDERIZAÇÃO ---

/**
 * Função auxiliar que gera o HTML de um card de atalho.
 * @returns {string} Uma string HTML contendo o card.
 */
function createCard(titulo, texto, link, cor, icone) {
    return `
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-${cor} shadow h-100 py-2">
                <a href="${link}" class="text-decoration-none">
                    <div class="card-body"><div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-${cor} text-uppercase mb-1">${titulo}</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">${texto}</div>
                        </div>
                        <div class="col-auto"><i class="fas fa-${icone} fa-2x text-gray-300"></i></div>
                    </div></div>
                </a>
            </div>
        </div>`;
}

/**
 * Função auxiliar que gera o HTML de um item do menu lateral.
 * @returns {string} Uma string HTML contendo o item de menu.
 */
function createSidebarLink(texto, link, icone) {
    return `<li class="nav-item"><a class="nav-link" href="${link}"><i class="fas fa-fw fa-${icone}"></i><span>${texto}</span></a></li>`;
}

// --- FUNÇÕES DE BUSCA DE PACIENTES ---

/** Função de conveniência para carregar a lista inicial de pacientes ativos. */
async function carregarPacientesIniciais() {
    await buscarPacientesNaApi('/pacientes');
}

/** * Função que lê o input de busca, determina se a busca é por nome ou CPF,
 * e monta a URL correta para a chamada da API.
 */
async function buscarPacientes() {
    const busca = document.getElementById('buscaInput').value.trim();
    let url;

    // Se o campo de busca estiver vazio, carrega todos os pacientes ativos.
    if (!busca) {
        url = '/pacientes';
    }
        // Expressão regular para verificar se o input contém apenas números, pontos ou traços.
    // Isso indica que o usuário provavelmente digitou um CPF.
    else if (/^[\d.-]+$/.test(busca)) {
        const cpfNumerico = busca.replace(/\D/g, ''); // Limpa a máscara para enviar só os números.
        // **NOTA TÉCNICA:** Para esta busca funcionar, é necessário que o backend
        // tenha um endpoint que aceite o parâmetro de busca 'cpf'. Ex: /pacientes?cpf=...
        url = `/pacientes?cpf=${encodeURIComponent(cpfNumerico)}`;
    }
    // Se não for um CPF, assume-se que é uma busca por nome.
    else {
        url = `/pacientes?nome=${encodeURIComponent(busca)}`;
    }

    await buscarPacientesNaApi(url);
}

/**
 * Função principal que executa a chamada fetch para a API,
 * mostra o feedback de "carregando" e renderiza o resultado na tabela.
 * @param {string} url - A URL da API a ser chamada.
 */
async function buscarPacientesNaApi(url) {
    const tbody = document.getElementById('listaResultados');
    tbody.innerHTML = '<tr><td colspan="3" class="text-center">Buscando...</td></tr>'; // Feedback de carregamento.

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`Erro na requisição: ${response.statusText}`);

        const pacientes = await response.json();
        tbody.innerHTML = ''; // Limpa a tabela para novos resultados.

        if (pacientes.length > 0) {
            pacientes.forEach(paciente => {
                // Cria uma nova linha na tabela para cada paciente encontrado.
                tbody.innerHTML += `
                    <tr>
                        <td>${paciente.nome}</td>
                        <td>${paciente.cpf}</td>
                        <td><a href="../prontuario/prontuario.html?pacienteId=${paciente.id}" class="btn btn-sm btn-outline-primary">Ver Prontuário</a></td>
                    </tr>`;
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center">Nenhum paciente encontrado.</td></tr>';
        }
    } catch (error) {
        // Tratamento de erro para falhas de rede ou da API.
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">Erro ao carregar pacientes.</td></tr>';
        console.error("Falha ao buscar pacientes:", error);
    }
}
