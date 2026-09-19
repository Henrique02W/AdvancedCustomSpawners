# 🐚 AdvancedCustomSpawners

[![Build](https://github.com/Henrique02W/AdvancedCustomSpawners/actions/workflows/build.yml/badge.svg)](https://github.com/Henrique02W/AdvancedCustomSpawners/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Henrique02W/AdvancedCustomSpawners?display_name=tag)](https://github.com/Henrique02W/AdvancedCustomSpawners/releases/latest)
![Minecraft](https://img.shields.io/badge/minecraft-26.2-brightgreen)
![Paper](https://img.shields.io/badge/paper-26.2-blue)
![Java](https://img.shields.io/badge/java-25-orange)
![Licença](https://img.shields.io/badge/licen%C3%A7a-Non--Commercial-blue)
![Vault](https://img.shields.io/badge/economia-Vault-yellow)

🇧🇷 Português · [🇺🇸 English](README.en.md)

> 🐚 Spawners customizados com upgrades, combustível, armazenamento interno e roteamento de drops.

---

## 📖 Sobre o Projeto

O **AdvancedCustomSpawners** substitui os spawners vanilla por uma versão muito mais completa: spawners portáteis que podem ser capturados, melhorados, alimentados com combustível, vinculados a containers para envio automático de drops e controlados via GUI, com hologramas e integração de economia.

O projeto foi pensado para ser:

- Altamente configurável (upgrades, combustíveis, mobs bloqueados, itens da GUI)
- Flexível no destino dos drops (armazenamento interno ou container vinculado)
- Integrado ao ecossistema do servidor (Vault, PlaceholderAPI)

---

## 🛠️ Funcionalidades

- 🥚 **Captura de mobs**: itens capturadores (preenchidos ou vazios) para definir o tipo de mob do spawner
- ⚙️ **7 upgrades independentes**, cada um com múltiplos níveis e custos configuráveis:
  * ⚡ Velocidade (delay mínimo/máximo de spawn)
  * 🔢 Quantidade (mobs por spawn)
  * 📏 Alcance (distância mínima de jogador para ativar)
  * 🌍 Condições (ignora regras ambientais do vanilla — luz, bloco, água, dia/noite)
  * 💎 Drops (multiplicador de itens dropados)
  * ✨ XP (multiplicador de experiência)
  * 🔥 Combustível (redução no consumo)
- 🔥 **Sistema de combustível**: carvão, carvão vegetal, blaze rod e balde de lava, cada um com duração e eficiência próprias
- 📦 **Armazenamento interno** de drops, com limite configurável de stacks
- 🔗 **Vínculo com containers**: roteia os drops automaticamente para um baú (ou outro container) a até uma distância configurável
- 🔀 **Modos de fallback** configuráveis quando o container alvo está cheio ou ausente (armazenamento interno, pausa ou drop no chão)
- 🖼️ **GUI completa** com botão liga/desliga, coleta de itens, vínculo/desvínculo, remoção e upgrades
- 💡 **Hologramas** exibindo informações do spawner
- 💰 Integração com **Vault** para custo de upgrades em dinheiro (com fallback para esmeraldas se o Vault não estiver disponível)
- 🚫 Lista de **mobs bloqueados** configurável (por padrão bloqueia Ender Dragon, Wither e Warden)
- 📋 Remoção preserva tipo, upgrades e combustível salvo no item devolvido
- 🧩 Integração opcional com **PlaceholderAPI**

---

## 🧰 Tecnologias Utilizadas

- ☕ Java 25
- 📦 Maven
- 🗺️ Paper 26.2
- 💰 Vault API (economia, opcional)
- 🧩 PlaceholderAPI (opcional)

---

## 🚀 Começando

### 📦 Requisitos

- Servidor **Paper 26.2**
- **Java 25** ou superior
- (Opcional) Vault + plugin de economia, e/ou PlaceholderAPI

> ℹ️ A partir da versão **2.0.0** o plugin suporta apenas o Minecraft 26.2 (Paper). A linha 1.x, feita para o 1.21.8, não recebe mais suporte.

### ⬇️ Instalação

1. Baixe o `.jar` mais recente em [Releases](https://github.com/Henrique02W/AdvancedCustomSpawners/releases/latest)
2. Coloque o arquivo na pasta `plugins/` do servidor
3. Inicie o servidor e edite `plugins/AdvancedCustomSpawners/config.yml` e `messages.yml`
4. Use `/spawner reload` para aplicar mudanças

### 🔄 Atualizando da versão 1.x (1.21.8)

- Faça backup da pasta `plugins/AdvancedCustomSpawners/` (principalmente o `data.yml`).
- O nome do plugin, as chaves internas e o formato do `data.yml` não mudaram, então spawners e itens existentes devem continuar sendo reconhecidos. Teste com uma cópia do servidor antes de atualizar o de produção.
- Atualize o servidor para Paper 26.2 com Java 25 e troque o `.jar` pela versão 2.x.

### 🔨 Compilando a partir do código

Requer **JDK 25** e Maven.

```
git clone https://github.com/Henrique02W/AdvancedCustomSpawners.git
cd AdvancedCustomSpawners
mvn package
```

O jar será gerado em `target/AdvancedCustomSpawners-<versão>.jar`.

---

## 🔑 Configuração

Edite `config.yml` para ajustar comportamento geral, combustíveis e upgrades:

```yaml
settings:
  internal-storage-max-stacks: 216
  fallback-when-target-full: "INTERNAL" # INTERNAL, PAUSE, DROP
  fallback-when-target-missing: "INTERNAL" # INTERNAL, DISABLE
  max-link-distance: 64
  economy:
    enabled: true
    require-vault: true
  blocked-mobs:
    - ENDER_DRAGON
    - WITHER
    - WARDEN

fuel:
  consume-per-spawn: 20
  items:
    COAL:
      duration: 1200
      efficiency: 1.0
    BLAZE_ROD:
      duration: 3600
      efficiency: 1.25

upgrades:
  speed:
    max-level: 5
    costs: [0, 8, 16, 32, 64, 128]
    min-delay: [200, 160, 120, 90, 65, 45]
    max-delay: [800, 650, 500, 380, 280, 200]
```

Todos os mobs vivos e "spawnáveis" são permitidos por padrão — adicione em `settings.blocked-mobs` apenas os que deseja bloquear. A GUI (materiais dos botões) e os textos dos itens também são totalmente customizáveis em `config.yml` e `messages.yml`.

---

## 🧠 Uso

O comando principal é `/spawner` (aliases: `/customspawner`, `/acs`).

- `/spawner give <player> <mob> [amount]` — entrega spawner customizado
- `/spawner give <player> <mob> [amount] egg` — entrega capturador de mob já preenchido
- `/spawner giveempty <player> [amount]` — entrega capturador vazio
- `/spawner reload` — recarrega as configurações
- `/spawner info` — mostra dados do spawner mirado
- `/spawner link` — inicia vínculo do spawner mirado com um container
- `/spawner unlink` — remove o vínculo
- `/spawner remove` — remove o spawner mirado e devolve ao jogador

A GUI possui um botão para ligar/desligar o spawner. O upgrade **Condições** faz o plugin ignorar regras ambientais do vanilla (luz, tipo de bloco, água, dia/noite), usando spawn direto controlado pelo próprio sistema.

### Permissões

| Permissão                 | Descrição                                   | Padrão |
| ------------------------- | ------------------------------------------- | ------ |
| `advancedspawners.use`    | Permite usar as GUIs de spawner customizado | `true` |
| `advancedspawners.give`   | Permite entregar spawners e capturadores    | `op`   |
| `advancedspawners.reload` | Permite recarregar a configuração           | `op`   |
| `advancedspawners.info`   | Permite ler informações do spawner          | `op`   |
| `advancedspawners.remove` | Permite remover spawners via GUI ou comando | `true` |
| `advancedspawners.link`   | Permite vincular spawners a containers      | `true` |
| `advancedspawners.bypass` | Ignora algumas restrições de tipo/colocação | `op`   |
| `advancedspawners.admin`  | Acesso a todos os comandos administrativos  | `op`   |

### Placeholders

Com PlaceholderAPI instalado:

- `%advancedspawners_total%`
- `%advancedspawners_stored_items%`
- `%advancedspawners_pending_routes%`

---

## 📂 Estrutura do Projeto

```
AdvancedCustomSpawners/
├── .github/
│   ├── workflows/            # CI (build) e release por tag
│   └── dependabot.yml
├── src/main/
│   ├── java/io/github/henrique02w/advancedcustomspawners/
│   │   ├── AdvancedCustomSpawnersPlugin.java
│   │   ├── command/          # /spawner
│   │   ├── config/           # leitura do config.yml
│   │   ├── gui/              # menus e cliques
│   │   ├── hologram/         # hologramas (TextDisplay)
│   │   ├── integration/      # PlaceholderAPI
│   │   ├── listener/         # eventos de spawner, blocos e mobs
│   │   ├── model/            # dados dos spawners
│   │   ├── service/          # itens, economia, links, mensagens, gerenciador
│   │   ├── storage/          # persistência (data.yml)
│   │   └── util/
│   └── resources/            # config.yml, messages.yml, plugin.yml
├── pom.xml
├── CHANGELOG.md
├── CONTRIBUTING.md
├── LICENSE.md / LICENSE_pt.md
└── README.md / README.en.md
```

---

## 🔒 Licença

Este projeto está sob a **Custom Non-Commercial Software License v1.0** (texto completo em [`LICENSE.md`](LICENSE.md), com versão em português em [`LICENSE_pt.md`](LICENSE_pt.md)).

⚠️ **Uso comercial é estritamente proibido.**

Você pode:

- Usar para fins pessoais
- Usar para fins educacionais
- Fazer forks e modificar

Você NÃO pode:

- Vender o plugin
- Monetizar qualquer parte do projeto

📩 Para uso comercial, entre em contato com o autor.

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Veja o [`CONTRIBUTING.md`](CONTRIBUTING.md).

## 🐛 Problemas (Issues)

Encontrou um bug ou tem uma sugestão?

- Abra uma [issue](https://github.com/Henrique02W/AdvancedCustomSpawners/issues)
- Descreva o problema claramente e informe a versão do Paper e do plugin
- Envie logs ou prints, se possível

---

## 📬 Contato

👤 **Henrique02W**

- GitHub: <https://github.com/Henrique02W>
- Discord: henrique02#7075

---

## ⭐ Apoie o Projeto

Se você gostou:

- ⭐ Dê uma estrela no repositório
- 🍴 Faça um fork
- 📢 Compartilhe com outras pessoas

---

> “Spawners de verdade, do jeito que sempre deveriam ter sido.”
