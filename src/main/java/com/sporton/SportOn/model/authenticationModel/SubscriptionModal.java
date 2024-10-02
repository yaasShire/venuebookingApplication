package com.sporton.SportOn.model.authenticationModel;

import com.sporton.SportOn.entity.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionModal {
private  SubscriptionType subscriptionType;
   private Double price=0.0;
}
