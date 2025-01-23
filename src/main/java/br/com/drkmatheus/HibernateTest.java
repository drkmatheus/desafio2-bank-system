package br.com.drkmatheus;

import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateTest {

    public static void main(String[] args) {
        // Teste para buscar um tipo de conta com ID 1
        testBuscarTipoConta(1);
    }

    public static void testBuscarTipoConta(int id) {
        Session session = null;
        try {
            // Abrir sessão Hibernate
            session = HibernateUtil.openSession();

            // Buscar o tipo de conta pelo ID
            BankAccountType tipoConta = session.get(BankAccountType.class, id);

            // Verificar se encontrou o tipo de conta
            if (tipoConta != null) {
                System.out.println("Tipo de conta encontrado: " + tipoConta.getTypeName());
            } else {
                System.out.println("Tipo de conta não encontrado para o ID: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
