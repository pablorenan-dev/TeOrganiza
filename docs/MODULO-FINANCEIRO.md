# Documentação — `App.java` e Módulo Financeiro

Documento explicando, classe por classe, o ponto de entrada da aplicação (`App.java`)
e todo o módulo **Financeiro** do projeto **TeOrganiza**.

O sistema é uma aplicação desktop em **Java Swing**, organizada em módulos
(pessoas, financeiro, estoque, eventos). Os dados ficam **em memória** (não há banco
de dados), e a arquitetura segue o padrão em camadas:

```
UI (Swing/Panels/Tabs)  →  Service (regras de negócio)  →  Repository (armazenamento)  →  Model (entidades)
```

---

## 1. `App.java` — Ponto de entrada

**Pacote:** `com.teamteorganiza`
**Arquivo:** [App.java](../src/main/java/com/teamteorganiza/App.java)

Classe que contém o método `main`. É responsável por **montar e ligar** toda a
aplicação. Não tem regra de negócio — apenas instancia e conecta os módulos.

### O que ela faz, passo a passo

1. **Define o Look & Feel do sistema** (linha 27) — faz a janela usar a aparência
   nativa do Windows. Se falhar, ignora silenciosamente.

2. **Sobe a interface na thread correta do Swing** com `SwingUtilities.invokeLater(...)`
   (linha 30). Toda criação de tela Swing deve rodar na *Event Dispatch Thread*.

3. **Cria os Services (camada de negócio) de cada módulo** (linhas 31–46), injetando
   neles os respectivos repositórios em memória. Para o financeiro:
   ```java
   FinanceiroService financeiroService = new FinanceiroService(
       new MensalidadeRepositoryEmMemoria()
   );
   ```

4. **Monta a navegação com `CardLayout`** (linhas 48–49). O `CardLayout` é como um
   baralho de cartas: vários painéis empilhados, mas só um visível por vez.

5. **Cria os painéis (telas) de cada módulo** (linhas 51–56), injetando os services.
   O painel do financeiro recebe **dois** services:
   ```java
   FinanceiroPanel financeiroPanel = new FinanceiroPanel(financeiroService, pessoaService);
   ```
   (precisa do `pessoaService` para mostrar o **nome** da pessoa a partir do ID.)

6. **Registra cada painel no `CardLayout` com um nome-chave** (linhas 58–63), ex.:
   `"HOME"`, `"FINANCEIRO"`, `"ESTOQUE"`.

7. **Liga os botões de navegação** (linhas 65–78). Cada `setOn...` recebe um
   `Runnable` que troca a carta visível. Exemplos:
   - `homePanel.setOnFinanceiro(() -> cards.show(root, "FINANCEIRO"))` — botão da
     home abre o financeiro.
   - `financeiroPanel.setOnVoltar(() -> cards.show(root, "HOME"))` — botão "Voltar"
     do financeiro retorna à home.

8. **Cria e exibe a janela principal** (`JFrame`, linhas 80–85): título "TeOrganiza",
   fecha o app ao fechar a janela, tamanho 950×620, centralizada na tela.

> **Resumo:** `App.java` é o *montador* (composition root). Ele cria as engrenagens
> (repos → services → painéis) e as conecta. Nenhuma regra de negócio mora aqui.

---

## 2. Visão geral do Módulo Financeiro

O módulo está dividido em três camadas/pacotes:

| Pacote | Responsabilidade |
|--------|------------------|
| `com.teamteorganiza.financeiro` | Service, repositórios e tipos auxiliares |
| `com.teamteorganiza.financeiro.model` | Entidades (os dados em si) |
| `com.teamteorganiza.financeiro.ui` | Telas Swing (painel + abas) |

O financeiro trabalha com **quatro fontes de dinheiro**, todas tratadas como
"lançamentos":

- **Mensalidades** — cobranças mensais por pessoa.
- **Movimentações** — entradas e despesas avulsas.
- **Vaquinhas** — campanhas de arrecadação com meta e contribuições.
- **Caixa de evento** — vendas de um evento que, ao fechar, viram uma entrada única.

---

## 3. Camada Model (entidades)

**Pacote:** `com.teamteorganiza.financeiro.model`

### 3.1 `Lancamento` (classe abstrata) — a base de tudo
[Lancamento.java](../src/main/java/com/teamteorganiza/financeiro/model/Lancamento.java)

Superclasse abstrata de **todo** registro financeiro. Define o que é comum a
qualquer lançamento:

- **Atributos:** `id`, `descricao`, `valor`, `data`, `tipo` (RECEITA/DESPESA).
- **`contadorId` estático** (linha 7): gera IDs únicos e sequenciais. Cada novo
  lançamento recebe `contadorId++`, então o ID é **global para o módulo inteiro**
  (mensalidade, venda, contribuição… todos compartilham a mesma sequência).
- **`mesDoLancamento()`** (linha 37): devolve o mês no formato `"AAAA-MM"`,
  extraído da data. Usado para agrupar/filtrar por mês.
- **`detalhar()`** (linha 41): método **abstrato** — cada subclasse formata sua
  própria linha de texto detalhada.
- **`toString()`**: formato genérico `#id [TIPO] descrição - R$ valor (data)`.

> É o coração do **polimorfismo** do módulo: o `FinanceiroService` e o `Dashboard`
> tratam tudo como `Lancamento`, sem se importar com o subtipo concreto.

### 3.2 `Mensalidade` (extends `Lancamento`)
[Mensalidade.java](../src/main/java/com/teamteorganiza/financeiro/model/Mensalidade.java)

Representa a cobrança mensal de uma pessoa. É sempre uma **RECEITA**.

- **Atributos próprios:** `pessoaId`, `mesReferencia` (ex.: `"2026-06"`),
  `vencimento`, `status`.
- Nasce com status **`EM_ABERTO`**.
- **`pagar()`**: muda o status para `PAGA`.
- **`estaAtrasada()`**: se não está paga e a data de vencimento já passou,
  marca como `ATRASADA` e retorna `true`.
- **`mesDoLancamento()`** é sobrescrito: usa o `mesReferencia` (e não a data de
  vencimento) para agrupar por mês.

### 3.3 `MovimentacaoFinanceira` (extends `Lancamento`)
[MovimentacaoFinanceira.java](../src/main/java/com/teamteorganiza/financeiro/model/MovimentacaoFinanceira.java)

Lançamento **avulso**: pode ser entrada (RECEITA) ou despesa (DESPESA). É o tipo
mais genérico, usado nas abas "Entradas" e "Despesas".

- **Atributo próprio:** `pessoaId` (quem movimentou o dinheiro).
- A data é sempre **hoje** (`LocalDate.now()`) no momento da criação.
- `pessoaId` é editável (`setPessoaId`), por isso essas movimentações podem ser
  editadas pela UI.
- **`detalhar()`**: mostra `+` para receita e `-` para despesa.

### 3.4 `Vaquinha`
[Vaquinha.java](../src/main/java/com/teamteorganiza/financeiro/model/Vaquinha.java)

⚠️ **Não** estende `Lancamento` — é um *agrupador* de contribuições (uma campanha).

- **Atributos:** `titulo`, `objetivo`, `meta` (valor alvo) e a lista de
  `contribuicoes`.
- **`contribuir(pessoaId, valor, descricao)`**: cria e adiciona uma
  `ContribuicaoVaquinha`, devolvendo-a.
- **`removerContribuicao(id)`**: remove pela identidade do lançamento.
- **`totalArrecadado()`**: soma de todas as contribuições.
- **`quantoFalta()`**: `meta − arrecadado` (nunca negativo).
- **`metaAtingida()`**: `true` se já bateu/passou da meta.
- **`listarContribuicoes()`**: imprime um resumo no console (uso de
  depuração/relatório textual).

### 3.5 `ContribuicaoVaquinha` (extends `Lancamento`)
[ContribuicaoVaquinha.java](../src/main/java/com/teamteorganiza/financeiro/model/ContribuicaoVaquinha.java)

Uma doação individual feita a uma vaquinha. Sempre **RECEITA**, data = hoje.

- **Atributo próprio:** `pessoaId` (o doador).
- Se a descrição vier vazia, gera automaticamente `"Doação pessoa X"`.
- É a entidade somada no ranking de **Top 3 doadores**.

### 3.6 `CaixaEvento`
[CaixaEvento.java](../src/main/java/com/teamteorganiza/financeiro/model/CaixaEvento.java)

⚠️ **Não** estende `Lancamento` — acumula as vendas do **evento corrente**.

- **Atributos:** `nomeEvento` e a lista de `vendas` (`VendaCaixa`).
- **`registrarVenda(...)`**, **`removerVenda(id)`**: gerenciam as vendas.
- **`total()`**: soma das vendas atuais.
- **`quantidadeVendas()`**: contagem.
- **`novoEvento(nome)`**: reinicia o caixa — troca o nome e **descarta** as vendas
  (usado no "fechar caixa").

### 3.7 `VendaCaixa` (extends `Lancamento`)
[VendaCaixa.java](../src/main/java/com/teamteorganiza/financeiro/model/VendaCaixa.java)

Uma venda individual dentro do caixa de um evento. Sempre **RECEITA**, data = hoje.

- **Atributo próprio:** `pessoaId`.
- Fica no `CaixaEvento` até o caixa ser fechado; nesse momento o **total** vira uma
  única `MovimentacaoFinanceira` de entrada.

### 3.8 Enums (tipos fechados)

| Enum | Valores | Uso |
|------|---------|-----|
| **`TipoLancamento`** [↗](../src/main/java/com/teamteorganiza/financeiro/model/TipoLancamento.java) | `RECEITA`, `DESPESA` | Classifica todo lançamento como entrada ou saída |
| **`StatusMensalidade`** [↗](../src/main/java/com/teamteorganiza/financeiro/model/StatusMensalidade.java) | `EM_ABERTO`, `PAGA`, `ATRASADA` | Situação de uma mensalidade |
| **`FormaPagamento`** [↗](../src/main/java/com/teamteorganiza/financeiro/model/FormaPagamento.java) | `PIX`, `DINHEIRO`, `CARTAO`, `FICHA` | Forma de pagamento (usada por `VendaEvento`) |

### 3.9 `Evento` e `VendaEvento` (modelo legado/paralelo)
[Evento.java](../src/main/java/com/teamteorganiza/financeiro/model/Evento.java) ·
[VendaEvento.java](../src/main/java/com/teamteorganiza/financeiro/model/VendaEvento.java)

Modelo de **venda de fichas** por evento, com preço fixo por ficha.

- **`Evento`**: tem `nome`, `data`, `precoFicha` e a lista de `vendas`.
  - **`venderFichas(quantidade, forma)`**: cria uma `VendaEvento` com
    `valor = quantidade × precoFicha`.
  - **`totalArrecadado()`**, **`totalFichasVendidas()`**, **`relatorioVendas()`**
    (imprime no console).
- **`VendaEvento`** (extends `Lancamento`): guarda `quantidadeFichas` e a
  `FormaPagamento`.

> ⚠️ **Observação:** `Evento`/`VendaEvento` **não** estão integrados ao
> `FinanceiroService` nem à UI atual — a aba "Caixa" usa `CaixaEvento`/`VendaCaixa`.
> São um modelo paralelo (provavelmente versão anterior ou base para evolução).

---

## 4. Camada Repository (armazenamento)

### 4.1 `Repository<T, ID>` (interface genérica comum)
[Repository.java](../src/main/java/com/teamteorganiza/common/Repository.java)
— pacote `com.teamteorganiza.common`

Contrato genérico de persistência reutilizado por todos os módulos:

```java
void salvar(T entidade);
Optional<T> buscarPorId(ID id);
List<T> listarTodos();
void remover(ID id);
```

### 4.2 `MensalidadeRepository` (interface)
[MensalidadeRepository.java](../src/main/java/com/teamteorganiza/financeiro/MensalidadeRepository.java)

Apenas **especializa** o contrato genérico para `Mensalidade` com ID `Integer`:
```java
public interface MensalidadeRepository extends Repository<Mensalidade, Integer> {}
```
Serve para o resto do código depender da **abstração** (interface), e não de uma
implementação concreta.

### 4.3 `MensalidadeRepositoryEmMemoria` (implementação)
[MensalidadeRepositoryEmMemoria.java](../src/main/java/com/teamteorganiza/financeiro/MensalidadeRepositoryEmMemoria.java)

Implementação concreta que guarda as mensalidades em um `ArrayList` na memória RAM.

- **`salvar`**: adiciona à lista.
- **`buscarPorId`**: procura por ID e devolve `Optional`.
- **`listarTodos`**: devolve uma **cópia** da lista (protege a lista interna).
- **`remover`**: remove pela igualdade de ID.

> Por ser "em memória", os dados são perdidos ao fechar o programa. Trocar por um
> repositório de banco de dados no futuro só exige criar outra classe que implemente
> `MensalidadeRepository` — nada mais muda.
>
> **Nota:** apenas as **mensalidades** usam repositório. As demais coleções
> (movimentações, vaquinhas, caixa) ficam em listas dentro do próprio
> `FinanceiroService`.

---

## 5. Camada Service (regras de negócio)

### 5.1 `FinanceiroService` — o cérebro do módulo
[FinanceiroService.java](../src/main/java/com/teamteorganiza/financeiro/FinanceiroService.java)

Centraliza **todas** as operações financeiras. A UI nunca mexe nos dados
diretamente: sempre passa por aqui.

**Estado interno (linhas 14–17):**
- `mensalidadeRepo` — repositório de mensalidades (injetado).
- `movimentacoes` — lista de entradas/despesas avulsas.
- `vaquinhas` — lista de vaquinhas.
- `caixa` — um único `CaixaEvento` (começa como "Evento 1").

**Principais grupos de métodos:**

| Grupo | Métodos | O que fazem |
|-------|---------|-------------|
| **Mensalidades** | `emitirMensalidade`, `pagarMensalidade`, `getMensalidades` | Emite cobrança, marca como paga, lista |
| **Entradas/Despesas** | `registrarEntrada`, `registrarDespesa`, `getEntradas`, `getDespesas`, `editarMovimentacao`, `removerMovimentacao`, `totalEntradas`, `totalDespesas` | CRUD e totais das movimentações avulsas |
| **Caixa** | `getCaixa`, `getVendasCaixa`, `totalCaixa`, `getNomeEvento`, `setNomeEvento`, `registrarVenda`, `editarVenda`, `removerVenda`, `fecharCaixa` | Gerencia o caixa do evento |
| **Vaquinhas** | `getVaquinhas`, `criarVaquinha`, `contribuir`, `editarContribuicao`, `removerContribuicao`, `top3Doadores` | Campanhas e ranking de doadores |
| **Extrato** | `todosLancamentos` | Consolida tudo num só lugar |

**Métodos que merecem destaque:**

- **`fecharCaixa(nomeNovoEvento)`** (linha 115): pega o total das vendas; se for
  positivo, cria **uma** `MovimentacaoFinanceira` de RECEITA descrevendo o evento e
  a quantidade de vendas; depois reinicia o caixa para um novo evento. É a "ponte"
  entre o caixa e o financeiro geral.

- **`top3Doadores()`** (linha 155): usa um `Map<pessoaId, total>` somando as
  contribuições de **todas** as vaquinhas (`merge` com `Double::sum`), ordena do
  maior para o menor e devolve os 3 primeiros como objetos `Doador`. Usa Streams.

- **`todosLancamentos()`** (linha 171): junta numa única lista as mensalidades, as
  movimentações, as contribuições de cada vaquinha e as vendas do caixa. É a base do
  **Extrato** e do **Dashboard** — graças ao polimorfismo, tudo são `Lancamento`.

- **`filtrar`** e **`somar`** (privados): auxiliares para separar por tipo e somar
  valores.

### 5.2 `Doador` (record)
[Doador.java](../src/main/java/com/teamteorganiza/financeiro/Doador.java)

Estrutura de dados **imutável** (record) com `pessoaId` e `total`. Resultado pronto
do cálculo de `top3Doadores()`, consumido pela aba de vaquinhas para montar o
ranking. Não tem comportamento, só carrega dados.

### 5.3 `Dashboard` (relatórios em console)
[Dashboard.java](../src/main/java/com/teamteorganiza/financeiro/Dashboard.java)

Gera **relatórios textuais** (via `System.out`) a partir do `FinanceiroService`.
Não faz parte da interface gráfica — é uma ferramenta de relatório/depuração.

- **`resumoMensal(mes)`**: soma receitas e despesas de um mês específico e imprime o
  saldo.
- **`gastosPorCategoria()`**: agrupa os valores por categoria (Mensalidades,
  Entradas, Despesas, Vaquinha, Caixa) separando receitas de despesas.
- **`prestacaoDeContas()`**: lista **todos** os lançamentos detalhados (via
  `detalhar()`) e fecha com total de entradas, saídas e resultado.
- **`categoriaDe(l)`** (privado): usa `instanceof` para descobrir a categoria de
  cada `Lancamento`.

---

## 6. Camada UI (telas Swing)

**Pacote:** `com.teamteorganiza.financeiro.ui`

### 6.1 `FinanceiroPanel` — a tela principal do módulo
[FinanceiroPanel.java](../src/main/java/com/teamteorganiza/financeiro/ui/FinanceiroPanel.java)

Painel raiz do financeiro. Monta a barra superior (botão **← Voltar**) e um
`JTabbedPane` com **5 abas**: Extrato, Entradas, Despesas, Vaquinhas e Caixa.

- Recebe `FinanceiroService` (dados) e `PessoaService` (para resolver nomes).
- **`nomeResolver`** (linha 31): função `id → nome da pessoa`. Cada aba a usa para
  exibir o nome em vez do número. Se o ID for `≤ 0`, mostra `"(caixa/evento)"`;
  se não achar, `"(id X não encontrado)"`.
- **`onChange = this::recarregarTodas`**: callback passado às abas. Sempre que algo
  muda numa aba, **todas** as abas se recarregam (mantém tudo sincronizado, ex.: uma
  venda nova já reflete no Extrato).
- **`recarregarTodas()`**: chama `recarregar()` de cada aba. Disparado também ao
  trocar de aba (`addChangeListener`).
- **`setOnVoltar(Runnable)`**: liga o botão Voltar (configurado no `App.java`).

### 6.2 `MovimentacaoTab` — aba reutilizável (Entradas e Despesas)
[MovimentacaoTab.java](../src/main/java/com/teamteorganiza/financeiro/ui/MovimentacaoTab.java)

Uma **única classe** que serve para duas abas: o construtor recebe um
`TipoLancamento` (`RECEITA` → "Entradas", `DESPESA` → "Despesas"). Boa reutilização
de código.

- **Layout:** tabela no centro (ID Pessoa, Nome, Valor, Descrição) + formulário
  embaixo (ID, Descrição, Valor) com botões **Criar / Editar / Deletar / Limpar**.
- **`criar()`**: lê ID e valor; conforme o tipo, chama `registrarEntrada` ou
  `registrarDespesa`.
- **`editar()` / `deletar()`**: agem sobre a linha selecionada na tabela.
- **`preencher()`**: ao clicar numa linha, copia os dados para o formulário.
- **`recarregar()`**: recarrega a tabela com `getEntradas()`/`getDespesas()`.
- **`lerId()` / `lerValor()`**: validam a entrada e avisam em caso de erro.

### 6.3 `CaixaTab` — aba do caixa de evento
[CaixaTab.java](../src/main/java/com/teamteorganiza/financeiro/ui/CaixaTab.java)

Gerencia as vendas do evento e o fechamento do caixa.

- **Topo:** campo do nome do evento + botão "Salvar nome", o **total do caixa** em
  destaque e o botão **"Fechar caixa / Novo evento"**.
- **Centro/Sul:** tabela de vendas + formulário CRUD (igual ao das movimentações).
- **`fecharCaixa()`**: se houver total, pergunta o nome do novo evento e chama
  `service.fecharCaixa(...)` — o total vira uma entrada e o caixa é zerado.
- **`recarregar()`**: atualiza a tabela e o rótulo de total; só reescreve o campo do
  nome se ele **não** estiver em foco (evita atrapalhar a digitação).

### 6.4 `VaquinhaTab` — aba de vaquinhas
[VaquinhaTab.java](../src/main/java/com/teamteorganiza/financeiro/ui/VaquinhaTab.java)

A aba mais complexa. Gerencia campanhas, suas contribuições e o ranking de doadores.

- **Topo:** `JComboBox` para escolher a vaquinha + botão **"Nova vaquinha"** + rótulo
  de informações (objetivo, arrecadado, meta, quanto falta).
- **Centro:** tabela de contribuições da vaquinha selecionada + à direita a tabela
  **"Top 3 doadores"** (todas as vaquinhas).
- **Sul:** formulário CRUD de contribuições.
- **`novaVaquinha()`**: abre um diálogo (título, objetivo, meta) e cria a vaquinha.
- **`atualizarTop3()`**: preenche o ranking com `service.top3Doadores()`.
- **`atualizandoCombo`** (flag): evita disparar eventos do combo enquanto ele é
  reconstruído programaticamente.
- **Classe interna `VaquinhaItem`**: *wrapper* para o combo exibir o **título** da
  vaquinha (via `toString()`) guardando a referência ao objeto real.

### 6.5 `ExtratoTab` — aba de extrato (somente leitura)
[ExtratoTab.java](../src/main/java/com/teamteorganiza/financeiro/ui/ExtratoTab.java)

Consolida **todos** os lançamentos numa única tabela, sem permitir edição.

- **Colunas:** ID, Tipo, ID Pessoa, Nome, Valor, Descrição, Data.
- **`recarregar()`**: percorre `service.todosLancamentos()`, monta a tabela e calcula
  o rodapé com **Entradas / Despesas / Saldo**.
- **`pessoaIdDe(l)`** (privado): usa `instanceof` para extrair o `pessoaId` de cada
  subtipo de `Lancamento` (mensalidade, movimentação, contribuição, venda).

### 6.6 `CampoUtil` — utilitário de conversão
[CampoUtil.java](../src/main/java/com/teamteorganiza/financeiro/ui/CampoUtil.java)

Classe utilitária `final` com construtor privado (não deve ser instanciada). Dois
métodos estáticos usados por todos os formulários do módulo:

- **`valor(texto)`**: converte texto em `double`, aceitando vírgula como separador
  decimal (`"10,50"` → `10.5`).
- **`id(texto)`**: converte texto em `int`.

Ambos lançam `NumberFormatException` em entrada inválida, que as abas capturam para
exibir um aviso ao usuário.

---

## 7. Como tudo se conecta (fluxo de exemplo)

Cenário: usuário registra uma **venda no caixa** e fecha o evento.

```
1. Usuário clica em "Criar" na aba Caixa (CaixaTab)
2. CaixaTab.criar() valida os campos com CampoUtil
3. Chama financeiroService.registrarVenda(id, descricao, valor)
4. FinanceiroService delega para caixa.registrarVenda(...) → cria um VendaCaixa
5. onChange.run() → FinanceiroPanel.recarregarTodas() → todas as abas recarregam
6. Usuário clica "Fechar caixa / Novo evento"
7. FinanceiroService.fecharCaixa() soma o total, cria 1 MovimentacaoFinanceira
   de RECEITA e zera o caixa (caixa.novoEvento)
8. A entrada agora aparece na aba Extrato (via todosLancamentos)
```

### Diagrama de dependências (simplificado)

```
            App.java  (monta tudo)
               │
               ▼
        FinanceiroPanel ──── PessoaService (resolve nomes)
               │
   ┌───────────┼─────────────┬───────────┐
   ▼           ▼             ▼           ▼
ExtratoTab  MovimentacaoTab VaquinhaTab CaixaTab   (+ CampoUtil)
   └───────────┴──────┬──────┴───────────┘
                      ▼
              FinanceiroService  ──► MensalidadeRepository(EmMemoria)
                      │
                      ▼
       Model: Lancamento (abstrata)
              ├── Mensalidade
              ├── MovimentacaoFinanceira
              ├── ContribuicaoVaquinha   (dentro de Vaquinha)
              ├── VendaCaixa             (dentro de CaixaEvento)
              └── VendaEvento            (dentro de Evento — paralelo)
```

---

## 8. Conceitos de POO presentes no módulo

| Conceito | Onde aparece |
|----------|--------------|
| **Herança** | Todos os lançamentos estendem `Lancamento` |
| **Polimorfismo** | `todosLancamentos()` trata tudo como `Lancamento`; `detalhar()` abstrato |
| **Abstração** | Classe `Lancamento` abstrata; interface `Repository` |
| **Encapsulamento** | Atributos privados com getters/setters; listas devolvidas como cópia |
| **Injeção de dependência** | Services recebem repositórios; painéis recebem services |
| **Composição** | `Vaquinha` contém `ContribuicaoVaquinha`; `CaixaEvento` contém `VendaCaixa` |
| **Genéricos** | `Repository<T, ID>` |
| **Record** | `Doador` (dados imutáveis) |
| **Enum** | `TipoLancamento`, `StatusMensalidade`, `FormaPagamento` |
