// Carrega o header
fetch("header.html")
  .then((res) => res.text())
  .then((data) => {
    document.getElementById("header-container").innerHTML = data;

    // Depois de inserir o header, adiciona os eventos do menu
    const links = document.querySelectorAll(".menu-link");
    links.forEach((link) => {
      link.addEventListener("click", function (e) {
        // Ativa link
        links.forEach((l) => l.classList.remove("active"));
        this.classList.add("active");

        // Se for link interno, faz scroll
        const href = this.getAttribute("href");
        if (href.startsWith("#")) {
          e.preventDefault();
          document.querySelector(href).scrollIntoView({ behavior: "smooth" });
        }
      });
    });
  });

//script especialidades//
// Aguarda o carregamento do DOM
document.addEventListener("DOMContentLoaded", function () {
  // Carregar o header (se você tiver um script para isso)
  // Exemplo: fetch('caminho/para/seu/header.html').then(response => response.text()).then(html => {
  //     document.getElementById('header-container').innerHTML = html;
  // });

  // Seleciona todos os cards de especialidade
  const cards = document.querySelectorAll(".card-especialidade");
  // Seleciona todos os botões de fechar modal
  const fecharBotoes = document.querySelectorAll(".fechar-modal");
  // Seleciona todos os modais
  const modais = document.querySelectorAll(".modal");

  // Adiciona evento de clique a cada card
  cards.forEach((card) => {
    card.addEventListener("click", function () {
      const modalId = this.dataset.modal; // Obtém o ID do modal do atributo data-modal
      const targetModal = document.getElementById(`modal-${modalId}`); // Constrói o ID completo

      if (targetModal) {
        targetModal.classList.add("active"); // Adiciona a classe 'active' para exibir e animar
      }
    });
  });

  // Adiciona evento de clique para fechar o modal
  fecharBotoes.forEach((btn) => {
    btn.addEventListener("click", function () {
      const modal = this.closest(".modal"); // Encontra o modal pai
      if (modal) {
        modal.classList.remove("active"); // Remove a classe 'active' para esconder e animar
      }
    });
  });

  // Adiciona evento para fechar o modal clicando fora dele
  modais.forEach((modal) => {
    modal.addEventListener("click", function (event) {
      // Se o clique foi diretamente no fundo do modal (não no modal-conteudo)
      if (event.target === this) {
        this.classList.remove("active"); // Remove a classe 'active'
      }
    });
  });

  // Evento para fechar o modal com a tecla ESC
  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      const activeModal = document.querySelector(".modal.active");
      if (activeModal) {
        activeModal.classList.remove("active");
      }
    }
  });

  // Adicione aqui outros scripts que você já tenha no main.js, como o do header
});
// Dispara em múltiplos eventos para garantir
document.addEventListener("DOMContentLoaded", scrollToBenefits);
window.addEventListener("load", scrollToBenefits);

// Adiciona listener para o link específico
document
  .getElementById("link-beneficios")
  ?.addEventListener("click", function () {
    sessionStorage.setItem("fromBenefitsLink", "true");
  });

//carregar o burguer

// Aguarda o carregamento completo do DOM antes de executar o script
document.addEventListener("DOMContentLoaded", function () {
  // Seleciona todos os links de menu com a classe 'menu-link'
  const menuLinks = document.querySelectorAll(".menu-link");
  // Obtém o nome do arquivo atual da URL (ex: "index.html")
  const currentPath = window.location.pathname.split("/").pop();

  // Itera sobre cada link de menu para adicionar a classe 'active' se for a página atual
  menuLinks.forEach((link) => {
    const linkPath = link.getAttribute("href"); // Obtém o atributo href do link
    if (linkPath === currentPath) {
      link.classList.add("active"); // Adiciona a classe 'active' ao link correspondente
    }
  });

  // --- Funcionalidade do Menu Hamburguer ---

  // Seleciona o elemento do menu hambúrguer
  const hamburger = document.querySelector(".hamburger-menu");
  // Seleciona o elemento da navegação mobile
  const mobileNav = document.querySelector(".mobile-nav");

  // Verifica se ambos os elementos existem antes de adicionar o event listener
  if (hamburger && mobileNav) {
    // Adiciona um listener de clique ao hambúrguer
    hamburger.addEventListener("click", function () {
      // Alterna a classe 'active' no próprio hambúrguer para a animação
      this.classList.toggle("active");
      // Alterna a classe 'active' na navegação mobile para mostrá-la/ocultá-la
      mobileNav.classList.toggle("active");
    });

    // Fechar o menu mobile ao clicar em um link dentro dele
    // (Útil se você tiver mais links no mobile-nav além de "Login Funcionário")
    mobileNav.querySelectorAll(".mobile-menu-link").forEach((link) => {
      link.addEventListener("click", () => {
        // Remove a classe 'active' do hambúrguer
        hamburger.classList.remove("active");
        // Remove a classe 'active' do menu mobile, fechando-o
        mobileNav.classList.remove("active");
      });
    });
  }
});
