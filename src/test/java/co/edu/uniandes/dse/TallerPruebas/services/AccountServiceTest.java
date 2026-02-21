package co.edu.uniandes.dse.TallerPruebas.services;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(AccountService.class)
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private TestEntityManager entityManager;
    private PodamFactory factory = new PodamFactoryImpl();
    private List<AccountEntity> accountList = new ArrayList<>();
    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from TransactionEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from PocketEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from AccountEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from UserEntity").executeUpdate();
    }

    private void insertData() {
        for (int i = 0; i < 3; i++) {
            AccountEntity accountEntity = factory.manufacturePojo(AccountEntity.class);
           
            accountEntity.setSaldo(1000.0 + (i * 100.0)); 
            accountEntity.setEstado("ACTIVA");
            entityManager.persist(accountEntity);
            accountList.add(accountEntity);
        }
        entityManager.flush();
    }

  
    @Test
    void testTransferBetweenAccountsSuccess() throws EntityNotFoundException, BusinessLogicException {
        AccountEntity origin = accountList.get(0);
        AccountEntity dest = accountList.get(1);

     
        origin.setSaldo(1000.0);
        dest.setSaldo(100.0);
        entityManager.merge(origin);
        entityManager.merge(dest);
        entityManager.flush();

        accountService.transferBetweenAccounts(origin.getId(), dest.getId(), 200.0);

        AccountEntity originAfter = entityManager.find(AccountEntity.class, origin.getId());
        AccountEntity destAfter = entityManager.find(AccountEntity.class, dest.getId());

        assertEquals(800.0, originAfter.getSaldo());
        assertEquals(300.0, destAfter.getSaldo());
    }

  
    @Test
    void testTransferBetweenAccountsInvalidOrigin() {
        AccountEntity dest = accountList.get(1);

        assertThrows(EntityNotFoundException.class, () -> {
            accountService.transferBetweenAccounts(0L, dest.getId(), 10.0);
        });
    }


    @Test
    void testTransferBetweenAccountsInvalidDestination() {
        AccountEntity origin = accountList.get(0);

        assertThrows(EntityNotFoundException.class, () -> {
            accountService.transferBetweenAccounts(origin.getId(), 0L, 10.0);
        });
    }


    @Test
    void testTransferBetweenAccountsSameAccount() {
        AccountEntity origin = accountList.get(0);

        assertThrows(BusinessLogicException.class, () -> {
            accountService.transferBetweenAccounts(origin.getId(), origin.getId(), 10.0);
        });
    }


    @Test
    void testTransferBetweenAccountsInsufficientFunds() throws EntityNotFoundException {
        AccountEntity origin = accountList.get(0);
        AccountEntity dest = accountList.get(1);

        origin.setSaldo(50.0);
        dest.setSaldo(100.0);
        entityManager.merge(origin);
        entityManager.merge(dest);
        entityManager.flush();

        assertThrows(BusinessLogicException.class, () -> {
            accountService.transferBetweenAccounts(origin.getId(), dest.getId(), 200.0);
        });

        AccountEntity originAfter = entityManager.find(AccountEntity.class, origin.getId());
        AccountEntity destAfter = entityManager.find(AccountEntity.class, dest.getId());

        assertEquals(50.0, originAfter.getSaldo());
        assertEquals(100.0, destAfter.getSaldo());
    }


    @Test
    void testTransferBetweenAccountsInvalidAmount() {
        AccountEntity origin = accountList.get(0);
        AccountEntity dest = accountList.get(1);

        assertThrows(BusinessLogicException.class, () -> {
            accountService.transferBetweenAccounts(origin.getId(), dest.getId(), 0.0);
        });
    }

 
    @Test
    void testTransferBetweenAccountsDestinationNullBalance() throws EntityNotFoundException, BusinessLogicException {
        AccountEntity origin = accountList.get(0);
        AccountEntity dest = accountList.get(1);

        origin.setSaldo(500.0);
        dest.setSaldo(null); 
        entityManager.merge(origin);
        entityManager.merge(dest);
        entityManager.flush();

        accountService.transferBetweenAccounts(origin.getId(), dest.getId(), 200.0);

        AccountEntity originAfter = entityManager.find(AccountEntity.class, origin.getId());
        AccountEntity destAfter = entityManager.find(AccountEntity.class, dest.getId());

        assertEquals(300.0, originAfter.getSaldo());
        assertEquals(200.0, destAfter.getSaldo());
    }
}
