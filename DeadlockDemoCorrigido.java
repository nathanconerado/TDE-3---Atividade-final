// Versão corrigida que evita o deadlock
public class DeadlockDemoCorrigido {
    // Os locks devem ser adquiridos em uma ordem global: LOCK_A antes de LOCK_B
    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            // T1 adquire A, depois B (Ordem correta)
            System.out.println("T1: Tentando adquirir LOCK_A...");
            synchronized (LOCK_A) {
                System.out.println("T1: LOCK_A adquirido.");
                dormir(50);
                System.out.println("T1: Tentando adquirir LOCK_B...");
                synchronized (LOCK_B) {
                    System.out.println("T1: LOCK_B adquirido. T1 concluiu.");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            // T2 também adquire A, depois B (Ordem correta)
            System.out.println("T2: Tentando adquirir LOCK_A...");
            synchronized (LOCK_A) {
                System.out.println("T2: LOCK_A adquirido.");
                dormir(50);
                System.out.println("T2: Tentando adquirir LOCK_B...");
                synchronized (LOCK_B) {
                    System.out.println("T2: LOCK_B adquirido. T2 concluiu.");
                }
            }
        });

        t1.start();
        t2.start();
        
        // Espera um tempo para a conclusão
        dormir(500); 
        System.out.println("Main: Verificação de conclusão. Se as threads concluíram, o deadlock foi evitado.");
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
