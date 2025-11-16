// Versão que reproduz o deadlock
public class DeadlockDemo {
    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            System.out.println("T1: Tentando adquirir LOCK_A...");
            synchronized (LOCK_A) {
                System.out.println("T1: LOCK_A adquirido.");
                dormir(50); // Simula algum trabalho enquanto segura o LOCK_A
                System.out.println("T1: Tentando adquirir LOCK_B...");
                synchronized (LOCK_B) {
                    System.out.println("T1: LOCK_B adquirido. T1 concluiu.");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            System.out.println("T2: Tentando adquirir LOCK_B...");
            synchronized (LOCK_B) {
                System.out.println("T2: LOCK_B adquirido.");
                dormir(50); // Simula algum trabalho enquanto segura o LOCK_B
                System.out.println("T2: Tentando adquirir LOCK_A...");
                synchronized (LOCK_A) {
                    System.out.println("T2: LOCK_A adquirido. T2 concluiu.");
                }
            }
        });

        t1.start();
        t2.start();
        
        // Espera um tempo para o deadlock ocorrer e o programa travar
        dormir(500); 
        System.out.println("Main: Verificação de travamento. Se as threads não concluíram, o deadlock ocorreu.");
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
