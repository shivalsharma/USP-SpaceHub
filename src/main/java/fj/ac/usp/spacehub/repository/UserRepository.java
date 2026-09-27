package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserRepository extends JpaRepository<UserAccount,Long>{
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    Optional<UserAccount> findByUserCodeIgnoreCase(String code);
    List<UserAccount> findByRoleAndActiveTrueOrderByName(UserRole role);
    List<UserAccount> findByRoleInOrderByName(Collection<UserRole> roles);
}
