// Tipos de contato suportados
const TIPOS_CONTATO = {
    CELULAR: 'CELULAR',
    TELEFONE: 'TELEFONE',
    EMAIL: 'EMAIL',
    WHATSAPP: 'WHATSAPP'
};

// Elementos DOM
let submitButton = null;

/**
 * Ponto de entrada principal
 */
document.addEventListener('DOMContentLoaded', function() {
    // 1. Validação de Sessão (apenas ADMIN)
    const usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado'));
    if (!usuarioLogado || !usuarioLogado.tipoUsuario.includes('ADMIN')) {
        showToast('Acesso negado. Redirecionando...', 'danger');
        setTimeout(() => window.location.href = '../dashboard/dashboard.html', 2000);
        return;
    }

    // 2. Inicialização
    initPage(usuarioLogado);
});

/**
 * Inicializa a página
 */
function initPage(usuarioLogado) {
    // Configura elementos
    document.getElementById('userName').textContent = usuarioLogado.nome || 'Administrador';
    renderizarSidebar(usuarioLogado.tipoUsuario);
    submitButton = document.querySelector('#formCadastroFuncionario button[type="submit"]');

    // Event listeners
    document.getElementById('formCadastroFuncionario').addEventListener('submit', cadastrarFuncionario);
    document.getElementById('logoutButton').addEventListener('click', handleLogout);
    document.getElementById('addContatoBtn').addEventListener('click', adicionarCampoDeContato);

    // Configura campos
    setupCpfField();
    adicionarCampoDeContato();

    // Validação em tempo real
    document.getElementById('cpf').addEventListener('blur', validarCpf);
    document.getElementById('dataNascimento').addEventListener('change', validarDataNascimento);
}

/**
 * Renderiza a sidebar usando o template SB Admin 2
 */
function renderizarSidebar(tipoUsuario) {
    const sidebarContainer = document.getElementById('sidebar-links');
    if (!sidebarContainer) return;

    sidebarContainer.innerHTML = '';
    const links = [];

    if (tipoUsuario.includes('ADMIN')) {
        links.push(`<li class="nav-item active">
            <a class="nav-link" href="../funcionarios/funcionarios.html">
                <i class="fas fa-fw fa-users-cog"></i><span>Gerenciar Equipe</span>
            </a>
        </li>`);
        links.push(`<li class="nav-item">
            <a class="nav-link" href="../relatorios/relatorios.html">
                <i class="fas fa-fw fa-chart-bar"></i><span>Relatórios</span>
            </a>
        </li>`);
    }

    links.push(`<li class="nav-item">
        <a class="nav-link" href="../agenda/agenda.html">
            <i class="fas fa-fw fa-calendar-alt"></i><span>Agenda</span>
        </a>
    </li>`);

    sidebarContainer.innerHTML = links.join('');
}

/**
 * Configura o campo de CPF com máscara
 */
function setupCpfField() {
    const cpfInput = document.getElementById('cpf');
    if (!cpfInput) return;

    cpfInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');

        if (value.length > 11) {
            value = value.substring(0, 11);
        }

        // Aplica máscara XXX.XXX.XXX-XX
        if (value.length > 9) {
            value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
        } else if (value.length > 6) {
            value = value.replace(/(\d{3})(\d{3})(\d{1,3})/, "$1.$2.$3");
        } else if (value.length > 3) {
            value = value.replace(/(\d{3})(\d{1,3})/, "$1.$2");
        }

        e.target.value = value;
    });
}

/**
 * Valida o formato do CPF usando algoritmo oficial
 */
function validarCpf() {
    const cpfInput = document.getElementById('cpf');
    const cpf = cpfInput.value.replace(/\D/g, '');

    // Validação básica de tamanho
    if (cpf.length !== 11) {
        cpfInput.classList.add('is-invalid');
        return false;
    }

    // Algoritmo de validação de CPF
    let soma = 0;
    let resto;

    if (/^(\d)\1+$/.test(cpf)) {
        cpfInput.classList.add('is-invalid');
        return false;
    }

    for (let i = 1; i <= 9; i++) {
        soma += parseInt(cpf.substring(i-1, i)) * (11 - i);
    }

    resto = (soma * 10) % 11;
    if ((resto === 10) || (resto === 11)) resto = 0;
    if (resto !== parseInt(cpf.substring(9, 10))) {
        cpfInput.classList.add('is-invalid');
        return false;
    }

    soma = 0;
    for (let i = 1; i <= 10; i++) {
        soma += parseInt(cpf.substring(i-1, i)) * (12 - i);
    }

    resto = (soma * 10) % 11;
    if ((resto === 10) || (resto === 11)) resto = 0;
    if (resto !== parseInt(cpf.substring(10, 11))) {
        cpfInput.classList.add('is-invalid');
        return false;
    }

    cpfInput.classList.remove('is-invalid');
    return true;
}

/**
 * Valida a data de nascimento (opcional)
 */
function validarDataNascimento() {
    const dataInput = document.getElementById('dataNascimento');
    const dataStr = dataInput.value;

    // Campo opcional, válido se vazio
    if (!dataStr) return true;

    const data = new Date(dataStr);
    const hoje = new Date();

    if (isNaN(data.getTime())) {
        dataInput.classList.add('is-invalid');
        return false;
    }

    // Valida se a data não é no futuro
    if (data > hoje) {
        dataInput.classList.add('is-invalid');
        return false;
    }

    // Cálculo de idade
    let idade = hoje.getFullYear() - data.getFullYear();
    const mesAtual = hoje.getMonth();
    const diaAtual = hoje.getDate();
    const mesNasc = data.getMonth();
    const diaNasc = data.getDate();

    // Ajusta idade se aniversário ainda não ocorreu
    if (mesNasc > mesAtual || (mesNasc === mesAtual && diaNasc > diaAtual)) {
        idade--;
    }

    // Valida idade mínima (14 anos)
    if (idade < 14) {
        dataInput.classList.add('is-invalid');
        return false;
    }

    dataInput.classList.remove('is-invalid');
    return true;
}

/**
 * Adiciona campo de contato
 */
function adicionarCampoDeContato() {
    const container = document.getElementById('contatosContainer');
    const novoContatoDiv = document.createElement('div');
    novoContatoDiv.className = 'contato-item mb-3 p-3 bg-light rounded';

    const inputId = `contato-valor-${Date.now()}`;

    novoContatoDiv.innerHTML = `
        <div class="row align-items-center">
            <div class="col-md-5 mb-2 mb-md-0">
                <select class="form-control tipo-contato" required
                    onchange="atualizarInputContato(this, '${inputId}')">
                    <option value="" disabled selected>Selecione o tipo</option>
                    <option value="${TIPOS_CONTATO.CELULAR}">Celular</option>
                    <option value="${TIPOS_CONTATO.TELEFONE}">Telefone</option>
                    <option value="${TIPOS_CONTATO.EMAIL}">E-mail</option>
                    <option value="${TIPOS_CONTATO.WHATSAPP}">WhatsApp</option>
                </select>
            </div>
            
            <div class="col-md-6 mb-2 mb-md-0">
                <input type="text" id="${inputId}" class="form-control valor-contato" 
                    placeholder="Informe o contato" required>
            </div>
            
            <div class="col-md-1 text-center">
                <button type="button" class="btn btn-danger btn-sm btn-remove-contato"
                    onclick="removerContato(this)">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `;

    container.appendChild(novoContatoDiv);
}

/**
 * Atualiza o input de contato baseado no tipo selecionado
 */
function atualizarInputContato(selectElement, inputId) {
    const inputElement = document.getElementById(inputId);
    if (!inputElement) return;

    const tipo = selectElement.value;
    inputElement.classList.remove('is-invalid');

    if (tipo === TIPOS_CONTATO.EMAIL) {
        inputElement.type = 'email';
        inputElement.placeholder = 'exemplo@dominio.com';
    } else {
        inputElement.type = 'tel';
        inputElement.placeholder = '(00) 00000-0000';

        // Adiciona máscara dinâmica
        inputElement.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');

            if (value.length > 11) value = value.substring(0, 11);

            // Aplica máscara (00) 00000-0000
            if (value.length > 10) {
                value = value.replace(/(\d{2})(\d{5})(\d{4})/, "($1) $2-$3");
            } else if (value.length > 6) {
                value = value.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
            } else if (value.length > 2) {
                value = value.replace(/(\d{2})(\d{0,5})/, "($1) $2");
            } else if (value.length > 0) {
                value = value.replace(/^(\d*)/, "($1");
            }

            e.target.value = value;
        });
    }
}

/**
 * Remove um campo de contato
 */
function removerContato(button) {
    // Não permite remover o último contato
    if (document.querySelectorAll('.contato-item').length <= 1) {
        showToast('É necessário pelo menos um contato', 'warning');
        return;
    }

    button.closest('.contato-item').remove();
}

/**
 * Valida todos os campos do formulário
 */
function validarFormulario() {
    let valido = true;

    // Valida campos obrigatórios
    const camposObrigatorios = ['nome', 'cpf', 'senha'];
    camposObrigatorios.forEach(id => {
        const campo = document.getElementById(id);
        if (!campo.value.trim()) {
            campo.classList.add('is-invalid');
            valido = false;
        } else {
            campo.classList.remove('is-invalid');
        }
    });

    // Valida tipo de funcionário
    const tipoSelect = document.getElementById('tipo');
    if (!tipoSelect.value) {
        tipoSelect.classList.add('is-invalid');
        valido = false;
    } else {
        tipoSelect.classList.remove('is-invalid');
    }

    // Valida CPF
    if (!validarCpf()) valido = false;

    // Valida data de nascimento (se preenchida)
    const dataNascimento = document.getElementById('dataNascimento');
    if (dataNascimento.value && !validarDataNascimento()) {
        valido = false;
    }

    // Valida contatos
    let contatoValido = false;
    document.querySelectorAll('.contato-item').forEach(item => {
        const tipo = item.querySelector('.tipo-contato').value;
        const valor = item.querySelector('.valor-contato').value.trim();

        if (tipo && valor) {
            if (validarFormatoContato(tipo, valor)) {
                contatoValido = true;
                item.querySelector('.valor-contato').classList.remove('is-invalid');
            } else {
                item.querySelector('.valor-contato').classList.add('is-invalid');
                valido = false;
            }
        } else {
            if (!tipo) item.querySelector('.tipo-contato').classList.add('is-invalid');
            if (!valor) item.querySelector('.valor-contato').classList.add('is-invalid');
            valido = false;
        }
    });

    if (!contatoValido) {
        valido = false;
    }

    return valido;
}

/**
 * Valida o formato de um contato
 */
function validarFormatoContato(tipo, valor) {
    const digitos = valor.replace(/\D/g, '');

    switch (tipo) {
        case TIPOS_CONTATO.EMAIL:
            return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor);

        case TIPOS_CONTATO.CELULAR:
        case TIPOS_CONTATO.WHATSAPP:
            return digitos.length === 11;

        case TIPOS_CONTATO.TELEFONE:
            return digitos.length === 10;

        default:
            return true;
    }
}

/**
 * Coleta os dados do formulário
 */
function coletarDadosFormulario() {
    // Obtém o valor do campo tipo
    const tipoSelect = document.getElementById('tipo');
    const tipo = tipoSelect.options[tipoSelect.selectedIndex].value;

    // Valida se um tipo foi selecionado
    if (!tipo) {
        tipoSelect.classList.add('is-invalid');
        return null;
    }

    return {
        nome: document.getElementById('nome').value.trim(),
        cpf: document.getElementById('cpf').value.replace(/\D/g, ''),
        dataNascimento: document.getElementById('dataNascimento').value || null,
        senha: document.getElementById('senha').value,
        tipo: tipo
    };
}

/**
 * Cadastra o funcionário
 */
async function cadastrarFuncionario(event) {
    event.preventDefault();

    // Impede múltiplos envios
    if (submitButton.disabled) return;

    // Valida o formulário
    if (!validarFormulario()) {
        showToast('Por favor, corrija os erros no formulário', 'danger');
        return;
    }

    // Coleta dados
    const dados = coletarDadosFormulario();

    // Verifica se os dados são válidos
    if (!dados) {
        showToast('Selecione um tipo de funcionário', 'warning');
        return;
    }

    // Mostra estado de carregamento
    toggleSubmitButton(true);

    try {
        // Envia para API
        const response = await fetch('/funcionarios', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(dados)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `Erro ${response.status}: ${response.statusText}`);
        }

        // Sucesso
        showToast('Funcionário cadastrado com sucesso!', 'success');

        // Limpa formulário
        event.target.reset();
        document.getElementById('contatosContainer').innerHTML = '';
        adicionarCampoDeContato();

    } catch (error) {
        // Tratamento de erro
        console.error('Erro no cadastro:', error);
        showToast(`Falha no cadastro: ${error.message}`, 'danger');
    } finally {
        toggleSubmitButton(false);
    }
}

/**
 * Alterna o estado do botão de envio
 */
function toggleSubmitButton(isLoading) {
    if (!submitButton) return;

    submitButton.disabled = isLoading;
    submitButton.innerHTML = isLoading
        ? '<span class="spinner-border spinner-border-sm"></span> Processando...'
        : '<i class="fas fa-save mr-2"></i> Cadastrar Funcionário';
}

/**
 * Mostra uma mensagem toast usando o SB Admin 2
 */
function showToast(message, type = 'info') {
    // Cria o container de toasts se não existir
    let toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.style.position = 'fixed';
        toastContainer.style.top = '20px';
        toastContainer.style.right = '20px';
        toastContainer.style.zIndex = '1060';
        document.body.appendChild(toastContainer);
    }

    // Cria o toast
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    toastContainer.appendChild(toast);

    // Inicializa e mostra o toast
    $(toast).toast({ delay: 5000 });
    $(toast).toast('show');

    // Remove o toast quando escondido
    $(toast).on('hidden.bs.toast', function () {
        toast.remove();
    });
}

/**
 * Manipula logout
 */
function handleLogout() {
    localStorage.clear();
    window.location.href = '../../login.html';
}

// Torna funções disponíveis globalmente para eventos inline
window.removerContato = removerContato;
window.atualizarInputContato = atualizarInputContato;