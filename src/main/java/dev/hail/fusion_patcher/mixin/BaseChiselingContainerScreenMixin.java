package dev.hail.fusion_patcher.mixin;

import com.supermartijn642.core.gui.widget.BaseContainerWidget;
import com.supermartijn642.core.gui.widget.Widget;
import com.supermartijn642.rechiseled.Rechiseled;
import com.supermartijn642.rechiseled.chiseling.ChiselingEntry;
import com.supermartijn642.rechiseled.chiseling.ChiselingRecipe;
import com.supermartijn642.rechiseled.packet.PacketChiselAll;
import com.supermartijn642.rechiseled.packet.PacketSelectEntry;
import com.supermartijn642.rechiseled.packet.PacketToggleConnecting;
import com.supermartijn642.rechiseled.screen.*;
import dev.hail.fusion_patcher.FusionPatcher;
import dev.hail.fusion_patcher.rechiseled.ScrollButtonWidget;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(BaseChiselingContainerScreen.class)
public abstract class BaseChiselingContainerScreenMixin <T extends BaseChiselingContainer> extends BaseContainerWidget<T> {
    @Shadow(remap = false)
    private ChiselingEntry getEntry(int index) {
        ChiselingRecipe recipe = this.container.currentRecipe;
        if (recipe == null) {
            return null;
        } else {
            return index >= 0 && index < recipe.getEntries().size() ? recipe.getEntries().get(index) : null;
        }
    }
    @Shadow(remap = false)
    private void selectEntry(int index) {
        Rechiseled.CHANNEL.sendToServer(new PacketSelectEntry(index));
    }
    @Shadow(remap = false)
    public static int previewMode = 0;
    @Shadow(remap = false)
    private ChiselAllWidget chiselAllWidget;
    @Shadow(remap = false)
    private void toggleConnecting() {
        Rechiseled.CHANNEL.sendToServer(new PacketToggleConnecting());
    }
    @Shadow(remap = false)
    private void chiselAll() {
        Rechiseled.CHANNEL.sendToServer(new PacketChiselAll());
    }

    @Unique
    private static int fusionPatcher$entryScroll = 0;

    public BaseChiselingContainerScreenMixin(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void addWidgets(){
        for(int row = 0; row < 5; row++){
            for(int column = 0; column < 5; column++){
                int index = row * 5 + column;
                int x = 9 + 20 * column;
                int y = 17 + 22 * row;
                this.addWidget(new EntryButtonWidget(x, y, 20, 22,
                        () -> this.getEntry(index + fusionPatcher$entryScroll * 5),
                        () -> this.container.currentEntry,
                        () -> this.selectEntry(index + fusionPatcher$entryScroll * 5),
                        () -> this.container.connecting));
            }
        }

        this.addWidget(new EntryPreviewWidget(117, 17, 68, 69, () -> {
            ChiselingEntry entry = this.container.currentEntry;
            if(entry == null)
                return null;
            return (this.container.connecting && entry.hasConnectingItem()) || !entry.hasRegularItem() ? entry.getConnectingItem() : entry.getRegularItem();
        }, () -> previewMode));
        Supplier<Boolean> enablePreviewButtons = () -> {
            ChiselingEntry entry = this.container.currentEntry;
            if(entry == null)
                return false;
            Item currentItem = (this.container.connecting && entry.hasConnectingItem()) || !entry.hasRegularItem() ? entry.getConnectingItem() : entry.getRegularItem();
            return currentItem instanceof BlockItem;
        };
        this.addWidget(new PreviewModeButtonWidget(193, 18, 19, 21, 2, () -> previewMode, enablePreviewButtons, () -> previewMode = 2));
        this.addWidget(new PreviewModeButtonWidget(193, 41, 19, 21, 1, () -> previewMode, enablePreviewButtons, () -> previewMode = 1));
        this.addWidget(new PreviewModeButtonWidget(193, 64, 19, 21, 0, () -> previewMode, enablePreviewButtons, () -> previewMode = 0));
        this.addWidget(new ConnectingToggleWidget(193, 99, 19, 21, () -> this.container.connecting, () -> this.container.currentEntry, this::toggleConnecting));
        this.addWidget(new ScrollButtonWidget(110,100,14,10, true, () -> fusionPatcher$scroll(true)));
        this.addWidget(new ScrollButtonWidget(110,110,14,10, false, () -> fusionPatcher$scroll(false)));
        this.chiselAllWidget = this.addWidget(new ChiselAllWidget(127, 99, 19, 21, () -> this.container.currentEntry, this::chiselAll));
    }

    @Inject(method = "getEntry", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectedScrollReset(int index, CallbackInfoReturnable<T> ci, ChiselingRecipe recipe) {
        if (recipe == null){
            fusionPatcher$entryScroll = 0;
        } else if (recipe.getEntries().size() < fusionPatcher$entryScroll * 5 + 20){
            fusionPatcher$entryScroll = 0;
        }
    }

    @Unique
    private void fusionPatcher$scroll(boolean up){
        ChiselingRecipe recipe = container.currentRecipe;
        FusionPatcher.LOGGER.debug(String.valueOf(fusionPatcher$entryScroll));
        if (recipe != null){
            if (up){
                if (fusionPatcher$entryScroll > 0){
                    fusionPatcher$entryScroll -= 1;
                } else {
                    fusionPatcher$entryScroll = 0;
                }
            } else {
                if (recipe.getEntries().size() > fusionPatcher$entryScroll * 5 + 25){
                    fusionPatcher$entryScroll += 1;
                }
            }
        }
    }
}
