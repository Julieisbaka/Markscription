package org.JulieISBaka.markscription.mixin;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.JulieISBaka.markscription.MarkdownBooks;
import org.JulieISBaka.markscription.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerMixin {
    @ModifyArg(method = "signBook", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/WrittenBookContent;<init>(Lnet/minecraft/server/network/Filterable;Ljava/lang/String;ILjava/util/List;Z)V"), index = 3)
    private List<Filterable<Component>> markscription$formatSignedPages(List<Filterable<Component>> pages) {
        return MarkdownBooks.formatPages(pages, ServerSettings.options());
    }
}
