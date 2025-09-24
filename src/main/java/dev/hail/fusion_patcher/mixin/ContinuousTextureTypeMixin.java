package dev.hail.fusion_patcher.mixin;

import com.supermartijn642.fusion.texture.types.continuous.ContinuousTextureType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ContinuousTextureType.class)
public class ContinuousTextureTypeMixin {
    @ModifyConstant(method = "deserialize", constant = @Constant(intValue = 10))
    private int modifyColumnsMaxValue(int value) {
        return 16;
    }
    @ModifyConstant(method = "deserialize", constant = @Constant(stringValue = "Property 'rows' must be greater than zero and less than 10!"))
    private String modifyColumnsMaxValueNotification(String value) {
        return "Property 'rows' must be greater than zero and less than 16!";
    }
}
