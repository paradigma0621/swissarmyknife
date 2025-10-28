Exemplo completo de **“Host + Plugins” com carregamento lazy** usando **JavaFX + JPMS + ServiceLoader**.
O host descobre plugins em runtime, **só cria a UI do plugin quando o usuário abre**, e descarta quando fecha a aba (deixa o GC liberar).

---

## Por que isso é “lazy”?

* O **host** apenas descobre os plugins (metadados) via `ServiceLoader`, **não cria as views**.
* A view do plugin é criada **somente quando você clica para abrir** (`createView()`).
* Ao **fechar a aba**, removemos o conteúdo; com o tempo o GC libera memória.
* O cache LRU (opcional) mantém até N views em memória para reabrir rápido; ao ultrapassar o limite, descarta a mais antiga.

---

## Como empacotar/rodar (Maven multi-módulo)

* Crie um projeto **multi-module**: `sak-parent` com submódulos `sak-api`, `sak-host`, `sak-clipboard`.
* Cada submódulo com seu `module-info.java` acima.
* Rode o host (por exemplo, com `javafx-maven-plugin` no módulo `sak-host`) e garanta que os **JARs dos plugins** estão no classpath/module-path do host (dependência de runtime ou pasta “plugins” se você quiser buscar dinamicamente).

## Como adicionar mais plugins

Crie um novo submódulo (ex.: sak-compare-ids) no parent:

module-info.java com requires com.example.sak.api e provides … with ….

Implemente ToolPlugin e uma view (FXML/Controller).

Adicione uma dependência runtime do novo plugin no sak-host/pom.xml (ou empacote o JAR do plugin na mesma pasta do host e garanta que está no module-path ao executar).

# 4) Padrões (e princípios) “embutidos” nesse desenho:

* **Plugin / Microkernel (Arquitetura de Plugins)**
  O app principal (host) é um kernel mínimo; as funcionalidades vêm como plugins carregados dinamicamente.

* **SPI (Service Provider Interface) + Service Loader**
  O `ToolPlugin` é a *interface de serviço* e o `ServiceLoader` descobre provedores em runtime.
  (Isso é frequentemente descrito como o **Provider pattern** viabilizado pelo SPI do Java.)

* **Inversão de Controle (IoC) / Dependency Inversion (D de SOLID)**
  O host depende de uma **abstração** (`ToolPlugin`), não de implementações concretas.
  Os plugins “plugam” no host via `provides … with …`.

* **Factory Method**
  O método `createView()` do plugin **fabrica** o `Node` (view) quando solicitado.
  Cada implementação decide *como* instanciar sua UI (FXML, código, etc.).

* **Lazy Initialization (Carregamento Tardio)**
  O host **não cria** as views no startup — só instância quando o usuário abre o plugin.

* **Caching (LRU Cache / Eviction Policy)**
  Mantém até *N* views em memória para reabrir rápido; ao exceder, descarta a mais antiga.

* **Separação de Módulos (JPMS / Module Pattern)**
  Cada submódulo tem `module-info.java`, reforçando encapsulamento e contratos claros.

* **Open/Closed Principle (O de SOLID)**
  O host fica **aberto à extensão** (novos plugins) e **fechado à modificação** (não precisa alterar o host).

Padrões **não essenciais**, mas que aparecem “de tabela”:

* **Command** (ações dos botões/atalhos disparam comandos; é mais um uso idiomático do que um design formal aqui).
* **Strategy** (cada plugin encapsula *a estratégia* de construir sua própria UI, mas o contrato é muito simples; se você adicionar métodos de “execute/transform”, vira um Strategy “de fato”).

Se você quiser, posso anotar no código (comentários breves) onde cada padrão entra — tipo “// SPI aqui”, “// Factory Method aqui”, “// Lazy aqui” — pra virar documentação viva do projeto.
