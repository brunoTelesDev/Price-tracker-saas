document.addEventListener("DOMContentLoaded", function () {
    carregarProdutos();
    verificarTelegram();

    document.getElementById("formAdicionarProduto").addEventListener("submit", function (e) {
        e.preventDefault();

        const btn = document.getElementById("btnSalvarProduto");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Robô buscando preço...';

        const novoProduto = {
            nome: document.getElementById("nomeProduto").value,
            url: document.getElementById("urlProduto").value,
            precoDesejado: parseFloat(document.getElementById("precoDesejadoProduto").value)
        };

        fetch("/api/produtos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(novoProduto)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Erro ao cadastrar produto");
            }
            return response.json();
        })
        .then(data => {
            document.getElementById("formAdicionarProduto").reset();
            const modalElement = document.getElementById("modalAdicionarProduto");
            const modal = bootstrap.Modal.getInstance(modalElement);
            modal.hide();

            carregarProdutos();
        })
        .catch(err => {
            alert("Erro ao cadastrar produto. Verifique a URL ou o valor digitado.");
        })
        .finally(() => {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-robot me-1"></i> Cadastrar e Buscar Preço';
        });
    });
});

// =========================================================
// TELEGRAM
// =========================================================

function verificarTelegram() {
    fetch("/api/telegram/status")
        .then(response => {
            if (!response.ok) {
                throw new Error("Erro ao verificar Telegram");
            }
            return response.json();
        })
        .then(conectado => {
            atualizarStatusTelegram(conectado);
        })
        .catch(error => {
            console.error("Erro ao verificar Telegram:", error);
            document.getElementById("telegramStatusText").innerText = "Erro";
        });
}

function atualizarStatusTelegram(conectado) {
    const statusText = document.getElementById("telegramStatusText");
    const dot = document.getElementById("telegramDot");
    const area = document.getElementById("telegramArea");

    if (conectado) {
        dot.className = "telegram-dot connected";
        statusText.className = "telegram-connected";
        statusText.innerHTML = '<i class="bi bi-telegram me-1"></i> Telegram conectado';
    } else {
        dot.className = "telegram-dot disconnected";
        statusText.className = "telegram-disconnected";
        statusText.innerHTML = '<i class="bi bi-telegram me-1"></i> Telegram não conectado';

        if (!document.getElementById("btnConectarTelegram")) {
            const botao = document.createElement("button");
            botao.id = "btnConectarTelegram";
            botao.className = "btn btn-telegram border-0 ms-2";
            botao.innerHTML = '<i class="bi bi-link-45deg me-1"></i> Conectar';
            botao.onclick = conectarTelegram;

            area.appendChild(botao);
        }
    }
}

function conectarTelegram() {
    const botao = document.getElementById("btnConectarTelegram");

    if (botao) {
        botao.disabled = true;
        botao.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
    }

    fetch("/api/telegram/conectar", {
        method: "POST"
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Erro ao gerar código");
        }
        return response.json(); // Consome o JSON retornado pelo controller
    })
    .then(data => {
        console.log("🔐 Código gerado:", data.codigo);

        if (data.link) {
            window.open(data.link, "_blank");
        }
    })
    .catch(error => {
        console.error("Erro ao conectar Telegram:", error);
        alert("Não foi possível gerar o código de conexão.");
    })
    .finally(() => {
        if (botao) {
            botao.disabled = false;
            botao.innerHTML = '<i class="bi bi-link-45deg me-1"></i> Conectar';
        }
    });
}

// =========================================================
// PRODUTOS
// =========================================================

function carregarProdutos() {
    fetch("/api/produtos")
        .then(res => res.json())
        .then(produtos => {
            document.getElementById("kpiProdutos").innerText = produtos.length;

            let alertasAtivos = 0;

            produtos.forEach(p => {
                if (
                    p.ativo !== false &&
                    p.precoAtual != null &&
                    p.precoDesejado != null &&
                    p.precoAtual <= p.precoDesejado
                ) {
                    alertasAtivos++;
                }
            });

            document.getElementById("kpiAlertas").innerText = alertasAtivos;

            const tbody = document.getElementById("tabelaProdutosBody");
            tbody.innerHTML = "";

            if (produtos.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center py-4 text-muted">
                            Nenhum produto cadastrado ainda. Clique em "Adicionar Produto" para começar!
                        </td>
                    </tr>
                `;
                return;
            }

            produtos.forEach(p => {
                const precoAtualFormatted = p.precoAtual != null
                    ? `R$ ${p.precoAtual.toFixed(2).replace(".", ",")}`
                    : "Lendo...";

                const precoDesejadoFormatted = p.precoDesejado != null
                    ? `R$ ${p.precoDesejado.toFixed(2).replace(".", ",")}`
                    : "R$ 0,00";

                const isAtivo = p.ativo !== false;

                const badgeStatus = isAtivo
                    ? `<span class="badge bg-success-subtle text-success border border-success rounded-pill">Monitorando</span>`
                    : `<span class="badge bg-secondary-subtle text-secondary border border-secondary rounded-pill">Pausado</span>`;

                const btnPausar = isAtivo
                    ? `<button onclick="alternarStatus(${p.id})" class="btn btn-sm btn-outline-warning" title="Pausar Monitoramento"><i class="bi bi-pause-fill"></i></button>`
                    : `<button onclick="alternarStatus(${p.id})" class="btn btn-sm btn-outline-success" title="Retomar Monitoramento"><i class="bi bi-play-fill"></i></button>`;

                tbody.innerHTML += `
                    <tr>
                        <td class="fw-semibold text-white">${p.nome}</td>
                        <td class="text-white">${precoAtualFormatted}</td>
                        <td class="text-accent fw-bold">${precoDesejadoFormatted}</td>
                        <td>${badgeStatus}</td>
                        <td class="text-end">
                            <div class="btn-group gap-1">
                                <a href="${p.url}" target="_blank" class="btn btn-sm btn-outline-light" title="Ver Link"><i class="bi bi-box-arrow-up-right"></i></a>
                                ${btnPausar}
                                <button onclick="excluirProduto(${p.id})" class="btn btn-sm btn-outline-danger" title="Excluir Produto"><i class="bi bi-trash-fill"></i></button>
                            </div>
                        </td>
                    </tr>
                `;
            });
        })
        .catch(err => console.error("Erro ao carregar produtos:", err));
}

function excluirProduto(id) {
    if (confirm("Tem certeza que deseja excluir este produto do monitoramento?")) {
        fetch(`/api/produtos/${id}`, { method: "DELETE" })
            .then(response => {
                if (response.ok) {
                    carregarProdutos();
                } else {
                    alert("Erro ao excluir produto.");
                }
            })
            .catch(err => console.error("Erro:", err));
    }
}

function alternarStatus(id) {
    fetch(`/api/produtos/${id}/status`, { method: "PATCH" })
        .then(response => {
            if (response.ok) {
                carregarProdutos();
            } else {
                alert("Erro ao alterar status.");
            }
        })
        .catch(err => console.error("Erro:", err));
}