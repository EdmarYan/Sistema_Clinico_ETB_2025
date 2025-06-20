/**
 * @file Lógica da página de cadastro de pacientes.
 * @description Implementa validações robustas, máscaras, gerenciamento dinâmico de contatos
 * e integração com API com tratamento detalhado de erros.
 * @version 2.0
 */

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
    // 1. Validação de Sessão
    const usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado'));
    if (!usuarioLogado || !(usuarioLogado.tipoUsuario.includes('ADMIN') || usuarioLogado.tipoUsuario.includes('RECEPCIONISTA'))) {
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
    document.getElementById('userName').textContent = usuarioLogado.nome || 'Usuário';
    renderizarSidebar(usuarioLogado.tipoUsuario);
    submitButton = document.getElementById('submitButton');

    // Event listeners
    document.getElementById('formCadastroPaciente').addEventListener('submit', cadastrarPaciente);
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
 * Renderiza a sidebar
 */
function renderizarSidebar(tipoUsuario) {
    const sidebarContainer = document.getElementById('sidebar-links');
    if (!sidebarContainer) return;

    sidebarContainer.innerHTML = '';
    const links = [];

    if (tipoUsuario.includes('ADMIN')) {
        links.push(`<li class="nav-item">
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

    if (tipoUsuario.includes('FISIOTERAPEUTA')) {
        links.push(`<li class="nav-item">
            <a class="nav-link" href="../meus-horarios/meus-horarios.html">
                <i class="fas fa-fw fa-clock"></i><span>Meus Horários</span>
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
 * Valida o formato do CPF
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
 * Valida a data de nascimento
 */
function validarDataNascimento() {
    const dataInput = document.getElementById('dataNascimento');
    const data = new Date(dataInput.value);
    const hoje = new Date();
    const dataStr = dataInput.value;

    // Verifica formato básico (YYYY-MM-DD)
    if (!/^\d{4}-\d{2}-\d{2}$/.test(dataStr)) {
        dataInput.classList.add('is-invalid');
        return false;
    }

    if (isNaN(data.getTime())) {
        dataInput.classList.add('is-invalid');
        return false;
    }

    // Valida se a data não é no futuro
    if (data > hoje) {
        dataInput.classList.add('is-invalid');
        return false;
    }

    // Cálculo de idade com variáveis mutáveis
    let idade = hoje.getFullYear() - data.getFullYear();
    const mesAtual = hoje.getMonth();
    const diaAtual = hoje.getDate();
    const mesNasc = data.getMonth();
    const diaNasc = data.getDate();

    // Ajusta idade se aniversário ainda não ocorreu este ano
    if (mesNasc > mesAtual || (mesNasc === mesAtual && diaNasc > diaAtual)) {
        idade--;
    }

    if (idade < 1) {
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
    inputElement.pattern = null; // Remove padrões anteriores

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

    // Valida campos principais
    const camposObrigatorios = ['nome', 'cpf', 'dataNascimento'];
    camposObrigatorios.forEach(id => {
        const campo = document.getElementById(id);
        if (!campo.value.trim()) {
            campo.classList.add('is-invalid');
            valido = false;
        }
    });

    // Valida CPF
    if (!validarCpf()) valido = false;

    // Valida data de nascimento
    if (!validarDataNascimento()) valido = false;

    // Valida contatos
    let contatoValido = false;
    document.querySelectorAll('.contato-item').forEach(item => {
        const tipo = item.querySelector('.tipo-contato').value;
        const valor = item.querySelector('.valor-contato').value.trim();

        if (tipo && valor) {
            if (validarFormatoContato(tipo, valor)) {
                contatoValido = true;
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
        document.getElementById('contato-validation').classList.remove('d-none');
        valido = false;
    } else {
        document.getElementById('contato-validation').classList.add('d-none');
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
    const contatos = [];

    document.querySelectorAll('.contato-item').forEach(item => {
        const tipo = item.querySelector('.tipo-contato').value;
        let valor = item.querySelector('.valor-contato').value.trim();

        // Remove formatação para números
        if (tipo !== TIPOS_CONTATO.EMAIL) {
            valor = valor.replace(/\D/g, '');
        }

        if (tipo && valor) {
            contatos.push({
                tipo,
                valor,
                principal: contatos.length === 0
            });
        }
    });

    return {
        nome: document.getElementById('nome').value.trim(),
        cpf: document.getElementById('cpf').value.replace(/\D/g, ''),
        dataNascimento: document.getElementById('dataNascimento').value,
        contatos
    };
}

/**
 * Cadastra o paciente
 */
async function cadastrarPaciente(event) {
    event.preventDefault();

    // Impede múltiplos envios simultâneos
    if (submitButton.disabled) return;

    // Valida o formulário
    if (!validarFormulario()) {
        showToast('Por favor, corrija os erros no formulário', 'danger');
        return;
    }

    // Coleta dados
    const dados = coletarDadosFormulario();

    // Mostra estado de carregamento
    toggleSubmitButton(true);

    try {
        // Envia para API
        const response = await fetch('/pacientes', {
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
        showToast('Paciente cadastrado com sucesso!', 'success');

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
        : '<i class="fas fa-save mr-2"></i> Cadastrar Paciente';
}

/**
 * Mostra uma mensagem toast
 */
function showToast(message, type = 'info') {
    // Implementação simplificada - poderia usar biblioteca ou componente customizado
    alert(`${type.toUpperCase()}: ${message}`);
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