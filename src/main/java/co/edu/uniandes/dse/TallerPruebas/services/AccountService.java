package co.edu.uniandes.dse.TallerPruebas.services;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
     @Transactional
    public void transferBetweenAccounts(Long originAccountId, Long destinationAccountId, Double monto)
            throws EntityNotFoundException, BusinessLogicException {

        log.info("Inicia transferencia de cuenta {} a cuenta {} por monto {}", originAccountId, destinationAccountId, monto);

        if (monto == null || monto <= 0) {
            throw new BusinessLogicException("El monto debe ser mayor a 0");
        }
        Optional<AccountEntity> originOpt = accountRepository.findById(originAccountId);
        if (originOpt.isEmpty()) {
            throw new EntityNotFoundException("La cuenta origen no existe");
        }
        Optional<AccountEntity> destOpt = accountRepository.findById(destinationAccountId);
        if (destOpt.isEmpty()) {
            throw new EntityNotFoundException("La cuenta destino no existe");
        }
        
        if (originAccountId.equals(destinationAccountId)) {
            throw new BusinessLogicException("La cuenta origen no puede ser la misma que la cuenta destino");
        }
        AccountEntity origin = originOpt.get();
        AccountEntity dest = destOpt.get();
        if (origin.getSaldo() == null || origin.getSaldo() < monto) {
            throw new BusinessLogicException("Fondos insuficientes en la cuenta origen");
        }
        origin.setSaldo(origin.getSaldo() - monto);
        Double saldoDestino = dest.getSaldo();
        if (saldoDestino == null) {
            saldoDestino = 0.0;
        }
        dest.setSaldo(saldoDestino + monto);
        accountRepository.save(origin);
        accountRepository.save(dest);
        log.info("Termina transferencia de cuenta {} a cuenta {}", originAccountId, destinationAccountId);
    }
    
}
