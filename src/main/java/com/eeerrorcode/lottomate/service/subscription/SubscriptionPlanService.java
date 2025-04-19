package com.eeerrorcode.lottomate.service.subscription;

import com.eeerrorcode.lottomate.domain.dto.subscription.SubscriptionPlanDto;

public interface SubscriptionPlanService {

  SubscriptionPlanDto getPlanByName(String plan);
  
}
