# Detalhes técnicos

Exemplo completo de **“Host + Plugins” com carregamento sob demanda (lazy)** usando **JavaFX + JPMS + ServiceLoader**.

O **SwissArmyKnife Host** descobre automaticamente os módulos (plugins) em tempo de execução, mas **só cria a interface gráfica do plugin quando o usuário o abre**.
Ao fechar a aba, o conteúdo é liberado para o **Garbage Collector (GC)**, mantendo o uso de memória leve mesmo com vários módulos disponíveis.

---

## Por que isso é “lazy”?

* O **host** apenas **descobre** os plugins via `ServiceLoader`, lendo seus metadados.
* A **view** de cada plugin (FXML/Controller) é **criada somente quando o usuário abre** a ferramenta (`createView()`).
* Ao **fechar a aba**, o conteúdo é removido e liberado pelo GC.
* Um **cache LRU** opcional mantém até *N* visualizações recentes em memória, para reabertura instantânea; ao ultrapassar o limite, descarta a mais antiga.

---

## Como o projeto está estruturado (Maven multi-módulo)

O SwissArmyKnife é um projeto **multi-módulo**, com a seguinte estrutura:

```
swissArmyKnife/
├── swissArmyKnife-api/         → Interface SPI (ToolPlugin)
├── swissArmyKnife-host/        → Aplicação principal (carregador de plugins)
├── swissArmyKnife-clipboard/   → Plugin de cópia/salvamento de senha
├── swissArmyKnife-scratch/     → Plugin de anotações rápidas
└── swissArmyKnife-comparator/  → Base para novos comparadores
```

### Passos gerais

1. O `swissArmyKnife-api` contém a interface `ToolPlugin`.
2. Cada plugin implementa essa interface e declara o `provides … with …` no seu `module-info.java`.
3. O `swissArmyKnife-host` usa o `ServiceLoader` para detectar automaticamente os plugins no `module-path`.
4. Ao abrir um plugin, o host chama `createView()`, que constrói a interface (via FXML ou código JavaFX).

---

## Como adicionar um novo plugin

1. Crie um novo submódulo, por exemplo `swissArmyKnife-myplugin`.

2. No `module-info.java`, declare:

   ```java
   requires paradigma0621.swissarmyknife.api;
   provides paradigma0621.swissarmyknife.api.ToolPlugin
       with paradigma0621.swissarmyknife.myplugin.MyPlugin;
   ```

3. Implemente a interface:

   ```java
   public class MyPlugin implements ToolPlugin {
       public String id() { return "myplugin"; }
       public String name() { return "My Plugin"; }
       public Node createView() { return FXMLLoader.load(getClass().getResource("MyPluginView.fxml")); }
   }
   ```

4. Adicione um FXML e um Controller para sua interface.

5. O host detectará automaticamente o novo módulo ao iniciar.

---

## Padrões de projeto e princípios aplicados

### Núcleo e extensões (Arquitetura de Plugins / Microkernel)

O **host** atua como um núcleo mínimo (kernel).
As funcionalidades vêm em **plugins independentes**, carregados dinamicamente.
Cada ferramenta — como *Clipboard*, *Comparator* ou *Scratch* — é um módulo separado.

---

### SPI (Service Provider Interface) + Service Loader

O `ToolPlugin` é a interface de serviço (contrato).
O `ServiceLoader` do Java descobre automaticamente todas as implementações disponíveis no `module-path`.

Esse é o **Provider Pattern**, que o SPI do Java implementa nativamente.

---

### Inversão de Controle (IoC) / Princípio da Inversão de Dependência (D de SOLID)

O **host não depende de implementações concretas**.
Ele trabalha apenas com a abstração `ToolPlugin`.
Os plugins se conectam ao host declarando “provides … with …”.

---

### Factory Method

O método `createView()` é um **Factory Method**.
Cada plugin decide como criar sua interface (FXML, código JavaFX, etc.).

---

### Lazy Initialization (carregamento sob demanda)

Nenhuma interface é criada no início.
A view só é construída quando o usuário **abre o plugin**.

---

### Caching (LRU Cache)

O host mantém um cache de até *N* visualizações em memória para reabrir rapidamente.
Ao exceder o limite, a view menos usada é removida.

---

### Modularização (JPMS / Module System)

Cada submódulo possui seu próprio `module-info.java`.
Isso garante **encapsulamento**, **controle de dependências** e **contratos explícitos** entre os módulos.

---

### Open/Closed Principle (O de SOLID)

O host é **aberto à extensão** (novos plugins) e **fechado à modificação** (não precisa ser alterado para suportá-los).

---

### Outros padrões presentes de forma complementar

* **Command:** ações dos botões e atalhos são comandos discretos executados pelo Controller.
* **Strategy:** cada plugin representa uma “estratégia” diferente de interface e comportamento, compartilhando o mesmo contrato (`ToolPlugin`).