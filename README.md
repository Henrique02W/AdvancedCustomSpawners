# 🐚 AdvancedCustomSpawners

![Licença](https://img.shields.io/badge/licença-Non--Commercial-blue)
![Status](https://img.shields.io/badge/status-ativo-success)
![Versão](https://img.shields.io/badge/versão-1.0.0-informational)
![Java](https://img.shields.io/badge/java-21-orange)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.8-brightgreen)
![Vault](https://img.shields.io/badge/economia-Vault-yellow)
![Contribuições](https://img.shields.io/badge/contribuições-bem--vindas-orange)

> 🐚 Spawners customizados com upgrades, combustível, armazenamento interno e roteamento de drops.

---

## 📖 Sobre o Projeto

O **AdvancedCustomSpawners** foi desenvolvido para substituir os spawners vanilla por uma versão muito mais completa: spawners portáteis que podem ser capturados, upgradados, alimentados com combustível, vinculados a containers para envio automático de drops, e controlados via GUI com hologramas e integração de economia.

O projeto foi pensado para ser:

* Altamente configurável (upgrades, combustíveis, mobs bloqueados, itens da GUI)
* Flexível no destino dos drops (armazenamento interno ou container vinculado)
* Integrado ao ecossistema do servidor (Vault, PlaceholderAPI)

---

## 🛠️ Funcionalidades

* 🥚 **Captura de mobs**: itens capturadores (preenchidos ou vazios) para definir o tipo de mob do spawner
* ⚙️ **7 upgrades independentes**, cada um com múltiplos níveis e custos configuráveis:
  * ⚡ Velocidade (delay mínimo/máximo de spawn)
  * 🔢 Quantidade (mobs por spawn)
  * 📏 Alcance (distância mínima de jogador para ativar)
  * 🌍 Condições (ignora regras ambientais do vanilla — luz, bloco, água, dia/noite)
  * 💎 Drops (multiplicador de itens dropados)
  * ✨ XP (multiplicador de experiência)
  * 🔥 Combustível (redução no consumo)
* 🔥 **Sistema de combustível**: carvão, carvão vegetal, blaze rod e balde de lava, cada um com duração e eficiência próprias
* 📦 **Armazenamento interno** de drops, com limite configurável de stacks
* 🔗 **Vínculo com containers**: roteia os drops automaticamente para um baú (ou outro container) a até uma distância configurável
* 🔀 **Modos de fallback** configuráveis quando o container alvo está cheio ou ausente (armazenamento interno, pausa, ou drop no chão)
* 🖼️ **GUI completa** com botão liga/desliga, coleta de itens, vínculo/desvínculo, remoção e upgrades
* 💡 **Hologramas** exibindo informações do spawner
* 💰 Integração com **Vault** para custo de upgrades em dinheiro (com fallback para esmeraldas se o Vault não estiver disponível)
* 🚫 Lista de **mobs bloqueados** configurável (por padrão bloqueia Ender Dragon, Wither e Warden)
* 📋 Remoção preserva tipo, upgrades e combustível salvo no item devolvido
* 🧩 Integração opcional com **PlaceholderAPI**

---

## 🧰 Tecnologias Utilizadas

* ☕ Java 21
* 📦 Maven e Gradle (ambos suportados)
* 🗺️ Paper 1.21.8
* 💰 Vault API (economia, opcional)
* 🧩 PlaceholderAPI (opcional)

---

## 📂 Estrutura do Projeto

```bash
AdvancedCustomSpawners/
├── src/
│   └── main/
│       ├── java/com/example/advancedspawners/
│       │   ├── AdvancedCustomSpawnersPlugin.java
│       │   ├── command/
│       │   │   └── SpawnerCommand.java
│       │   ├── config/
│       │   │   └── PluginConfig.java
│       │   ├── gui/
│       │   │   ├── GuiListener.java
│       │   │   ├── GuiManager.java
│       │   │   └── SpawnerGuiHolder.java
│       │   ├── hologram/
│       │   │   └── HologramManager.java
│       │   ├── integration/
│       │   │   └── PlaceholderIntegration.java
│       │   ├── listener/
│       │   │   └── SpawnerListener.java
│       │   ├── model/
│       │   │   ├── BlockLocation.java
│       │   │   ├── DropMode.java
│       │   │   ├── FallbackMode.java
│       │   │   ├── SpawnerData.java
│       │   │   └── UpgradeType.java
│       │   ├── service/
│       │   │   ├── EconomyService.java
│       │   │   ├── ItemFactory.java
│       │   │   ├── LinkService.java
│       │   │   ├── MessageService.java
│       │   │   └── SpawnerManager.java
│       │   ├── storage/
│       │   │   └── SpawnerStorage.java
│       │   └── util/
│       │       └── Keys.java
│       └── resources/
│           ├── config.yml
│           ├── messages.yml
│           └── plugin.yml
├── pom.xml
├── build.gradle
├── settings.gradle
├── LICENSE.md
├── LICENSE_pt.md
└── README.md
```

---

## 🚀 Começando

### 📦 Requisitos

* Servidor Paper para Minecraft 1.21.8
* Java 21 ou superior
* (Opcional) Vault + plugin de economia, e/ou PlaceholderAPI

---

### ⚙️ Instalação

```bash
# Clonar o repositório
git clone https://github.com/Henrique02W/AdvancedCustomSpawners.git

# Entrar na pasta
cd AdvancedCustomSpawners
```

Compile com Maven:

```bash
mvn package
```

O jar será gerado em `target/AdvancedCustomSpawners-1.0.0.jar`. Ou, com Gradle:

```bash
gradle build
```

Coloque o arquivo gerado na pasta `/plugins` do seu servidor.

---

### 🔑 Configuração

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

### ▶️ Executando

1. Coloque o jar em `plugins/`
2. Inicie o servidor Paper 1.21.8
3. Edite `plugins/AdvancedCustomSpawners/config.yml` e `messages.yml`
4. Use `/spawner reload`

---

## 🧠 Uso

* `/spawner give <player> <mob> [amount]` — entrega spawner customizado
* `/spawner give <player> <mob> [amount] egg` — entrega capturador de mob já preenchido
* `/spawner giveempty <player> [amount]` — entrega capturador vazio
* `/spawner reload` — recarrega as configurações
* `/spawner info` — mostra dados do spawner mirado
* `/spawner link` — inicia vínculo do spawner mirado com um container
* `/spawner unlink` — remove o vínculo
* `/spawner remove` — remove o spawner mirado e devolve ao jogador

A GUI possui um botão para ligar/desligar o spawner. O upgrade **Condições** faz o plugin ignorar regras ambientais do vanilla (luz, tipo de bloco, água, dia/noite), usando spawn direto controlado pelo próprio sistema.

### Permissões

| Permissão | Descrição | Padrão |
|---|---|---|
| `advancedspawners.use` | Permite usar as GUIs de spawner customizado | `true` |
| `advancedspawners.give` | Permite entregar spawners e capturadores | `op` |
| `advancedspawners.reload` | Permite recarregar a configuração | `op` |
| `advancedspawners.info` | Permite ler informações do spawner | `op` |
| `advancedspawners.remove` | Permite remover spawners via GUI ou comando | `true` |
| `advancedspawners.link` | Permite vincular spawners a containers | `true` |
| `advancedspawners.admin` | Acesso a todos os comandos administrativos | `op` |

### Placeholders

Com PlaceholderAPI instalado:

* `%advancedspawners_total%`
* `%advancedspawners_stored_items%`
* `%advancedspawners_pending_routes%`

---

## 🔒 Licença

Este projeto está sob uma **Licença Personalizada Não Comercial**.

⚠️ **Uso comercial é estritamente proibido.**

Você pode:

* Usar para fins pessoais
* Usar para fins educacionais
* Fazer forks e modificar

Você NÃO pode:

* Vender o plugin
* Monetizar qualquer parte do projeto

📩 Para uso comercial, entre em contato com o autor.

---

## 🤝 Contribuindo

Contribuições são bem-vindas!

## 🐛 Problemas (Issues)

Encontrou um bug ou tem uma sugestão?

* Abra uma issue
* Descreva o problema claramente
* Envie logs ou prints, se possível

---

## 📬 Contato

👤 **Henrique02W**

* GitHub: https://github.com/Henrique02W
* Discord: henrique02#7075

---

## ⭐ Apoie o Projeto

Se você gostou:

* ⭐ Dê uma estrela no repositório
* 🍴 Faça um fork
* 📢 Compartilhe com outras pessoas

---

> “Spawners de verdade, do jeito que sempre deveriam ter sido.”
