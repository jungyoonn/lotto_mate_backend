package com.eeerrorcode.lottomate.repository;

import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    
    Optional<SocialAccount> findByProviderAndSocialId(Provider provider, String socialId);
    
    List<SocialAccount> findByUser(User user);
    
    Optional<SocialAccount> findByUserAndProvider(User user, Provider provider);
    
    boolean existsByProviderAndSocialId(Provider provider, String socialId);
}