/**
 * Interceptor Global de Requisições (Fetch API)
 * Este script monitora TODAS as requisições feitas pelo frontend.
 * Se o backend retornar um Erro 401 (Acesso Negado / Token Expirado),
 * ele automaticamente limpa a sessão e desloga o usuário.
 */
const originalFetch = window.fetch;

window.fetch = async function() {
    // Executa a requisição original normalmente
    const response = await originalFetch.apply(this, arguments);
    
    // Verifica se a resposta foi 401 (Unauthorized) e se já não estamos na página de login
    if (response.status === 401 && !window.location.pathname.includes('login.html')) {
        console.warn("Token JWT expirado ou inválido. Redirecionando para login...");
        
        // Limpa os dados velhos da sessão
        localStorage.removeItem('token');
        localStorage.removeItem('usuarioLogado');
        
        // Avisa o usuário
        alert("Sua sessão expirou por questões de segurança. Por favor, faça login novamente.");
        
        // Redireciona para a página de login raiz
        window.location.href = '/login.html'; 
    }
    
    return response;
};
