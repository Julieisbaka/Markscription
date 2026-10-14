package org.JulieISBaka.markscription.mixin.client;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.JulieISBaka.markscription.MarkdownBooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookViewScreen.BookAccess.class)
abstract class BookAccessMixin {
    @Inject(method = "fromItem", at = @At("RETURN"), cancellable = true)
    private static void markscription$renderWrittenBook(
            ItemStack stack, CallbackInfoReturnable<BookViewScreen.BookAccess> callback) {
        BookViewScreen.BookAccess access = callback.getReturnValue();
        if (access != null && stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            callback.setReturnValue(new BookViewScreen.BookAccess(
                    access.pages().stream().map(MarkdownBooks::renderPage).toList()));
        }
    }
}
