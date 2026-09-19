# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.
O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto usa [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [2.0.0] - Não lançado

### ⚠️ Mudanças que quebram compatibilidade
- O plugin agora suporta **apenas Paper 26.2** e exige **Java 25**. A linha 1.x (Paper 1.21.8, Java 21) não recebe mais suporte.
- O pacote Java foi renomeado de `com.example.advancedspawners` para `io.github.henrique02w.advancedcustomspawners`. Isso só afeta quem dependia das classes do plugin como API; o nome do plugin, as chaves internas e o formato do `data.yml` não mudaram.

### Alterado
- `paper-api` atualizado para `26.2.build.121-stable`; `api-version` do `plugin.yml` passou para `26.2`.
- Itens da GUI com `custom-model-data` agora usam o `CustomModelDataComponent` (API atual) em vez do método legado por inteiro.
- Autor e site corretos no `plugin.yml`.
- Filtragem de recursos do Maven restrita ao `plugin.yml`.
- `README.md` reescrito (requisitos, atualização a partir da 1.x, estrutura) e `README.en.md` adicionado.
- Permissão `advancedspawners.bypass` documentada e `advancedspawners.link` incluída em `advancedspawners.admin`.

### Corrigido
- Vault/economia não era detectado quando o plugin de economia ativava depois deste plugin: o provedor era lido uma única vez no enable. Agora é resolvido sob demanda.
- O console informa se a economia foi conectada (ao iniciar e em `/spawner reload`).

### Removido
- `build.gradle` e `settings.gradle`: o projeto passa a usar somente Maven.

### Infraestrutura
- GitHub Actions: build a cada push/PR e release automática ao enviar uma tag `vX.Y.Z`, com o JAR anexado.
- Dependabot para dependências Maven e Actions.
- `maven-enforcer-plugin` garantindo JDK 25+ na compilação.

## [1.0.0]

- Versão inicial para Paper 1.21.8: spawners customizados com upgrades, combustível, armazenamento interno, roteamento de drops, GUI, hologramas, Vault e PlaceholderAPI.
