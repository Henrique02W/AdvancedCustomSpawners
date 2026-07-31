# AdvancedCustomSpawners

Plugin Paper 1.21.8 / Java 21 para spawners customizados com upgrades, combustível, armazenamento interno, redirecionamento de drops para containers, captura de mob, Vault, hologramas e PlaceholderAPI opcional.

## Build

Com Maven:

```bash
mvn package
```

O jar será gerado em:

```text
target/AdvancedCustomSpawners-1.0.0.jar
```

Com Gradle, caso tenha Gradle instalado:

```bash
gradle build
```

## Instalação

1. Coloque o jar em `plugins/`.
2. Inicie o servidor Paper 1.21.8.
3. Edite `plugins/AdvancedCustomSpawners/config.yml` e `messages.yml`.
4. Use `/spawner reload`.

## Comandos

- `/spawner give <player> <mob> [amount]` entrega spawner customizado.
- `/spawner give <player> <mob> [amount] egg` entrega capturador de mob ja preenchido.
- `/spawner giveempty <player> [amount]` entrega capturador vazio.
- `/spawner reload` recarrega configs.
- `/spawner info` mostra dados do spawner mirado.
- `/spawner link` inicia vínculo do spawner mirado com um container.
- `/spawner unlink` remove vínculo.
- `/spawner remove` remove o spawner mirado e devolve ao jogador.

## Permissões

- `advancedspawners.use`
- `advancedspawners.link`
- `advancedspawners.give`
- `advancedspawners.reload`
- `advancedspawners.info`
- `advancedspawners.remove`
- `advancedspawners.admin`

## Vault

Se `settings.economy.enabled` estiver ativo, upgrades usam a economia do Vault. Caso `settings.economy.require-vault` esteja `false`, o plugin volta para custo em esmeraldas quando Vault nao estiver disponivel.

## Mobs

Todos os mobs vivos e spawnaveis sao permitidos por padrao. Para bloquear algum tipo, adicione o nome em `settings.blocked-mobs` no `config.yml`.

## Spawners Removidos

Ao remover ou quebrar um spawner customizado, o item devolvido preserva tipo, upgrades e combustivel salvo.

## Controle

A GUI possui um botao para ligar/desligar o spawner. O upgrade `Condicoes` faz o plugin ignorar regras ambientais do vanilla, como luz, tipo de bloco, agua ou dia/noite, usando spawn direto controlado pelo proprio sistema.

## Placeholders

Com PlaceholderAPI instalado:

- `%advancedspawners_total%`
- `%advancedspawners_stored_items%`
- `%advancedspawners_pending_routes%`
