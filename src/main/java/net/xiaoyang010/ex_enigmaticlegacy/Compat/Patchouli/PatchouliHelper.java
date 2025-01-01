package net.xiaoyang010.ex_enigmaticlegacy.Compat.Patchouli;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vazkii.patchouli.client.base.PersistentData;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

import javax.annotation.Nullable;

public class PatchouliHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MODID = "ex_enigmaticlegacy";

    @Nullable
    protected static Book getBook(ResourceLocation location) {
        if (location == null) {
            LOGGER.error("Attempted to get book with null ResourceLocation");
            return null;
        }

        try {
            return BookRegistry.INSTANCE.books.get(location);
        } catch (Exception e) {
            LOGGER.error("Failed to get book for location: " + location, e);
            return null;
        }
    }

    public static Book getLegacy() {
        ResourceLocation bookLocation = ForgeRegistries.ITEMS.getKey(ModItems.BIBLE.get());
        Book book = getBook(bookLocation);

        if (book == null) {
            LOGGER.error("Failed to get legacy book instance");
            throw new IllegalStateException("Legacy book not found");
        }

        return book;
    }

    private static void setEntryState(ResourceLocation entryLocation, boolean read) {
        try {
            Book theBook = getLegacy();
            if (theBook == null || theBook.getContents() == null) {
                LOGGER.error("Book or book contents is null");
                return;
            }

            BookEntry entry = theBook.getContents().entries.get(entryLocation);
            if (entry == null) {
                LOGGER.error("Entry not found for location: " + entryLocation);
                return;
            }

            PersistentData.BookData data = PersistentData.data.getBookData(theBook);
            if (data == null || data.viewedEntries == null) {
                LOGGER.error("Book data or viewed entries is null");
                return;
            }

            String entryId = String.valueOf(entry.getId());
            if (entryId == null) {
                LOGGER.error("Entry ID is null for location: " + entryLocation);
                return;
            }

            if (read && !data.viewedEntries.contains(entryId)) {
                data.viewedEntries.add(ResourceLocation.tryParse(entryId));
                entry.markReadStateDirty();
                LOGGER.debug("Marked entry as read: " + entryLocation);
            } else if (!read && data.viewedEntries.contains(entryId)) {
                data.viewedEntries.remove(entryId);
                entry.markReadStateDirty();
                LOGGER.debug("Marked entry as unread: " + entryLocation);
            }
        } catch (Exception e) {
            LOGGER.error("Error setting entry state for: " + entryLocation, e);
        }
    }

    public static void markEntryUnread(ResourceLocation entryLocation) {
        if (entryLocation == null) {
            LOGGER.error("Attempted to mark null entry as unread");
            return;
        }
        setEntryState(entryLocation, false);
    }

    public static void markEntryRead(ResourceLocation entryLocation) {
        if (entryLocation == null) {
            LOGGER.error("Attempted to mark null entry as read");
            return;
        }
        setEntryState(entryLocation, true);
    }

    public static void markEverythingRead() {
        try {
            Book theBook = getLegacy();
            if (theBook == null || theBook.getContents() == null) {
                LOGGER.error("Book or contents is null while marking everything read");
                return;
            }

            theBook.getContents().entries.keySet().forEach(PatchouliHelper::markEntryRead);
            LOGGER.info("Marked all entries as read");
        } catch (Exception e) {
            LOGGER.error("Error marking everything as read", e);
        }
    }

    public static void markEverythingUnread() {
        try {
            Book theBook = getLegacy();
            if (theBook == null || theBook.getContents() == null) {
                LOGGER.error("Book or contents is null while marking everything unread");
                return;
            }

            theBook.getContents().entries.keySet().forEach(PatchouliHelper::markEntryUnread);
            LOGGER.info("Marked all entries as unread");
        } catch (Exception e) {
            LOGGER.error("Error marking everything as unread", e);
        }
    }

    public static boolean isEntryAvailable(ResourceLocation entryLocation) {
        try {
            Book book = getLegacy();
            if (book == null || book.getContents() == null) {
                return false;
            }

            BookEntry entry = book.getContents().entries.get(entryLocation);
            return entry != null;  // 如果条目存在则认为可用
        } catch (Exception e) {
            LOGGER.error("Error checking entry availability: " + entryLocation, e);
            return false;
        }
    }

    public static int getReadEntryCount() {
        try {
            Book book = getLegacy();
            if (book == null) {
                return 0;
            }

            PersistentData.BookData data = PersistentData.data.getBookData(book);
            return data != null && data.viewedEntries != null ? data.viewedEntries.size() : 0;
        } catch (Exception e) {
            LOGGER.error("Error getting read entry count", e);
            return 0;
        }
    }

    public static void markCategoryRead(String category) {
        try {
            Book book = getLegacy();
            if (book == null || book.getContents() == null) {
                LOGGER.error("Book or contents is null while marking category read");
                return;
            }

            book.getContents().entries.forEach((location, entry) -> {
                if (entry.getCategory().getId().getPath().equals(category)) {
                    markEntryRead(location);
                }
            });
            LOGGER.info("Marked all entries in category " + category + " as read");
        } catch (Exception e) {
            LOGGER.error("Error marking category as read: " + category, e);
        }
    }
}