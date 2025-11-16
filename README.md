Relatório Final - TDE3: Concorrência, Deadlocks e Semáforos







Parte 1: O Jantar dos Filósofos

1.1. Dinâmica do Problema e Surgimento do Impasse

O problema do Jantar dos Filósofos é um clássico exemplo de concorrência em sistemas operacionais, proposto por Edsger Dijkstra. Ele modela a necessidade de alocação de recursos em um sistema onde múltiplos processos (os filósofos) competem por recursos limitados (os garfos) 1
.

Cinco filósofos estão sentados em uma mesa circular, alternando entre os estados de pensar e comer. Para comer, cada filósofo precisa de dois garfos: o que está à sua esquerda e o que está à sua direita. O problema surge porque os garfos são recursos compartilhados e limitados.

O protocolo ingênuo de "pegar primeiro o garfo da esquerda, depois o da direita" leva diretamente ao impasse (deadlock). Se todos os cinco filósofos ficarem simultaneamente com fome e pegarem o garfo à sua esquerda, todos estarão segurando um recurso e esperando por outro que está sendo segurado por seu vizinho.

As quatro condições de Coffman necessárias para que um deadlock ocorra são satisfeitas neste cenário ingênuo 2
:

1.
Exclusão Mútua: Os garfos são recursos não compartilháveis.

2.
Manter e Esperar (Hold and Wait): Cada filósofo segura um garfo e espera pelo outro.

3.
Não Preempção: Um garfo não pode ser tomado à força.

4.
Espera Circular (Circular Wait): Existe uma cadeia de espera circular.

1.2. Protocolo para Evitar Impasse (Hierarquia de Recursos)

Para evitar o impasse, é necessário negar pelo menos uma das quatro condições de Coffman. A estratégia escolhida é a Hierarquia de Recursos, que elimina a condição de Espera Circular 1
.

Neste protocolo, impõe-se uma ordem global de aquisição dos recursos (os garfos). Cada garfo recebe um índice único (de 0 a N-1). O filósofo é forçado a adquirir primeiro o garfo com o menor índice entre os dois de que precisa, e só depois o garfo com o maior índice.

Ao impor uma ordem parcial fixa sobre os recursos, garantimos que não pode existir uma cadeia de espera circular. O último filósofo na "fila" de recursos (o que precisa do garfo de maior índice) sempre conseguirá adquirir seus dois garfos, quebrando o ciclo de espera.

1.3. Pseudocódigo do Protocolo (Hierarquia de Recursos)

O pseudocódigo a seguir demonstra a lógica para $N=5$ filósofos.

Dados:

•
$N = 5$ filósofos

•
Garfos $0..N-1$ (Garfo $i$ fica entre Filósofo $i$ e Filósofo $(i+1) \pmod N$)

Plain Text


Para cada Filósofo p:
    // Determina a ordem de aquisição baseada no índice do garfo
    left_garfo  = min(garfo_esquerda(p), garfo_direita(p))
    right_garfo = max(garfo_esquerda(p), garfo_direita(p))

    Loop:
        estado[p] <- "pensando"
        pensar()

        estado[p] <- "com fome"

        // 1. Adquire o garfo de menor índice (left_garfo)
        adquirir(left_garfo) // Bloqueia até o garfo estar livre

        // 2. Adquire o garfo de maior índice (right_garfo)
        adquirir(right_garfo) // Bloqueia até o garfo estar livre

        estado[p] <- "comendo"
        comer()

        // 3. Libera os garfos
        liberar(right_garfo)
        liberar(left_garfo)





Parte 2: Threads e Semáforos - Contador Concorrente

O objetivo desta parte é demonstrar uma condição de corrida e corrigi-la utilizando um semáforo binário justo (java.util.concurrent.Semaphore).

2.1. Demonstração da Condição de Corrida (CorridaSemControle.java)

A versão sem sincronização expõe a condição de corrida, onde o incremento da variável count++ não é atômico, resultando na perda de incrementos.

Parâmetro
Valor
Número de Threads (T)
8
Incrementos por Thread (M)
250.000
Valor Esperado (T x M)
2.000.000


Resultado da Execução:

Plain Text


Esperado=2000000, Obtido=576457, Tempo=0.043s


O valor obtido é incorreto, confirmando a perda de incrementos.

2.2. Correção com Semáforo Binário Justo (CorridaComSemaphore.java)

A correção é implementada utilizando um Semaphore(1, true), que atua como um lock de exclusão mútua justo (FIFO), garantindo que apenas uma thread por vez acesse a seção crítica.

Resultado da Execução:

Plain Text


Esperado=2000000, Obtido=2000000, Tempo=48.860s


O valor obtido é o valor correto esperado, demonstrando que o semáforo eliminou a condição de corrida.

2.3. Discussão sobre Trade-off e happens-before

O uso do semáforo introduz um trade-off: a correção da condição de corrida é alcançada ao custo de uma redução drástica no throughput (vazão) e um aumento no tempo de execução (de 0.043s para 48.860s). Isso ocorre porque as threads agora executam a seção crítica de forma serializada.

O Semaphore também oferece garantias de consistência de memória do tipo happens-before 3
. A operação release() de uma thread tem uma relação happens-before com a operação acquire() subsequente de outra thread. Isso garante que a thread que entra na seção crítica sempre veja o valor mais recente e correto de count, preservando a visibilidade e a ordem entre as threads.




Parte 3: Deadlock com Locks

3.1. Reprodução do Deadlock (DeadlockDemo.java)

O cenário de deadlock é criado com duas threads (T1 e T2) e dois locks (LOCK_A e LOCK_B), onde a ordem de aquisição é invertida, satisfazendo a condição de Espera Circular.

Logs que evidenciam o travamento:

Plain Text


T1: Tentando adquirir LOCK_A...
T1: LOCK_A adquirido.
T2: Tentando adquirir LOCK_B...
T2: LOCK_B adquirido.
T1: Tentando adquirir LOCK_B...
T2: Tentando adquirir LOCK_A...
Main: Verificação de travamento. Se as threads não concluíram, o deadlock ocorreu.


O programa trava, pois T1 segura LOCK_A e espera por LOCK_B, enquanto T2 segura LOCK_B e espera por LOCK_A.

3.2. Implementação Corrigida e Explicação (DeadlockDemoCorrigido.java)

A correção é feita impondo uma ordem global de aquisição de recursos (LOCK_A antes de LOCK_B) em ambas as threads, o que remove a condição de Espera Circular 2
.

Logs que evidenciam a conclusão:

Plain Text


T1: Tentando adquirir LOCK_A...
T1: LOCK_A adquirido.
T2: Tentando adquirir LOCK_A...
T1: Tentando adquirir LOCK_B...
T1: LOCK_B adquirido. T1 concluiu.
T2: LOCK_A adquirido.
T2: Tentando adquirir LOCK_B...
T2: LOCK_B adquirido. T2 concluiu.
Main: Verificação de conclusão. Se as threads concluíram, o deadlock foi evitado.


O programa conclui corretamente. A estratégia aplicada é a Hierarquia de Recursos, análoga à solução do Jantar dos Filósofos, quebrando o ciclo de espera e garantindo o progresso do sistema.

