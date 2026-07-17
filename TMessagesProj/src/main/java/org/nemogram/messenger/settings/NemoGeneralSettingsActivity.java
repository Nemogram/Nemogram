package org.nemogram.messenger.settings;

import android.text.TextUtils;
import android.view.View;

import androidx.core.text.HtmlCompat;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;
import java.util.Locale;

import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.PopupHelper;
import org.nemogram.messenger.translator.Translator;
import org.nemogram.messenger.translator.TranslatorApps;

public class NemoGeneralSettingsActivity extends BaseNemoSettingsActivity {

    private final int ipv6Row = rowId++;

    private final int showOriginalRow = rowId++;
    private final int translatorTypeRow = rowId++;
    private final int translatorExternalAppRow = rowId++;
    private final int translationProviderRow = rowId++;
    private final int translationTargetRow = rowId++;
    private final int doNotTranslateRow = rowId++;
    private final int autoTranslateRow = rowId++;

    private final int accentAsNotificationColorRow = rowId++;
    private final int useStockNotificationIconRow = rowId++;
    private final int silenceNonContactsRow = rowId++;

    private final int hideGiftsRow = rowId++;
    private final int musicViewAlternativeLayoutRow = rowId++;
    private final int nameOrderRow = rowId++;
    private final int idTypeRow = rowId++;

    private final int disabledInstantCameraRow = rowId++;
    private final int askBeforeCallRow = rowId++;
    private final int openArchiveOnPullRow = rowId++;
    private final int alwaysShowDownloadIconRow = rowId++;

    private CharSequence getTranslationProvider() {
        var providers = Translator.getProviders();
        var names = providers.first;
        var types = providers.second;
        if (names == null || types == null) {
            return "";
        }
        int index = types.indexOf(NemoConfig.translationProvider);
        if (index < 0) {
            return "";
        } else {
            return names.get(index);
        }
    }

    private CharSequence getTranslationTarget() {
        var language = NemoConfig.translationTarget;
        CharSequence value;
        if (language.equals("app")) {
            value = LocaleController.getString(R.string.TranslationTargetApp);
        } else {
            Locale locale = Locale.forLanguageTag(language);
            if (!TextUtils.isEmpty(locale.getScript())) {
                value = HtmlCompat.fromHtml(locale.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY);
            } else {
                value = locale.getDisplayName();
            }
        }
        return value;
    }

    private CharSequence getRestrictedLanguages() {
        var langCodes = Translator.getRestrictedLanguages();
        CharSequence value;
        if (langCodes.size() == 1) {
            Locale locale = Locale.forLanguageTag(langCodes.get(0));
            if (!TextUtils.isEmpty(locale.getScript())) {
                value = HtmlCompat.fromHtml(locale.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY);
            } else {
                value = locale.getDisplayName();
            }
        } else {
            value = LocaleController.formatPluralString("Languages", langCodes.size());
        }
        return value;
    }

    private CharSequence getTranslatorType() {
        return switch (NemoConfig.transType) {
            case NemoConfig.TRANS_TYPE_TG -> LocaleController.getString(R.string.TranslatorTypeTG);
            case NemoConfig.TRANS_TYPE_EXTERNAL ->
                    LocaleController.getString(R.string.TranslatorTypeExternal);
            default -> LocaleController.getString(R.string.TranslatorTypeNemo);
        };
    }

    private CharSequence getTranslatorExternalApp() {
        var app = TranslatorApps.getTranslatorApp();
        return app == null ? "" : app.title;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.Connection)));
        items.add(UItem.asCheck(ipv6Row, LocaleController.getString(R.string.PreferIPv6)).slug("ipv6").setChecked(NemoConfig.preferIPv6));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.Translator)));
        items.add(TextSettingsCellFactory.of(translatorTypeRow, LocaleController.getString(R.string.TranslatorType), getTranslatorType()).slug("translatorType"));
        if (NemoConfig.transType != NemoConfig.TRANS_TYPE_EXTERNAL) {
            if (NemoConfig.transType == NemoConfig.TRANS_TYPE_NEMO) {
                items.add(UItem.asCheck(showOriginalRow, LocaleController.getString(R.string.TranslatorShowOriginal)).slug("showOriginalRow").setChecked(NemoConfig.showOriginal));
            }
            items.add(TextSettingsCellFactory.of(translationProviderRow, LocaleController.getString(R.string.TranslationProviderShort), getTranslationProvider()).slug("translationProvider"));
            items.add(TextSettingsCellFactory.of(translationTargetRow, LocaleController.getString(R.string.TranslationTarget), getTranslationTarget()).slug("translationTarget"));
            items.add(TextSettingsCellFactory.of(doNotTranslateRow, LocaleController.getString(R.string.DoNotTranslate), getRestrictedLanguages()).slug("doNotTranslate"));
            items.add(UItem.asCheck(autoTranslateRow, LocaleController.getString(R.string.AutoTranslate), LocaleController.getString(R.string.AutoTranslateAbout)).slug("autoTranslate").setChecked(NemoConfig.autoTranslate));
        } else {
            items.add(TextSettingsCellFactory.of(translatorExternalAppRow, LocaleController.getString(R.string.TranslationProviderShort), getTranslatorExternalApp()).slug("translatorExternalApp"));
        }
        items.add(UItem.asShadow(LocaleController.getString(R.string.TranslateMessagesInfo1)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.Notifications)));
        items.add(UItem.asCheck(accentAsNotificationColorRow, LocaleController.getString(R.string.AccentAsNotificationColor)).slug("accentAsNotificationColor").setChecked(NemoConfig.accentAsNotificationColor));
        items.add(UItem.asCheck(useStockNotificationIconRow, LocaleController.getString(R.string.UseStockNotificationIcon)).slug("useStockNotificationIcon").setChecked(NemoConfig.useStockNotificationIcon));
        items.add(UItem.asCheck(silenceNonContactsRow, LocaleController.getString(R.string.SilenceNonContacts)).slug("silenceNonContacts").setChecked(NemoConfig.silenceNonContacts));
        items.add(UItem.asShadow(LocaleController.getString(R.string.SilenceNonContactsAbout)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.UserColorTabProfile)));
        items.add(UItem.asCheck(hideGiftsRow, LocaleController.getString(R.string.HideGifts)).slug("hideGifts").setChecked(NemoConfig.hideGifts));
        items.add(UItem.asCheck(musicViewAlternativeLayoutRow, LocaleController.getString(R.string.MusicViewAlternativeLayout)).slug("musicViewAlternativeLayout").setChecked(NemoConfig.musicViewAlternativeLayout));
        items.add(TextSettingsCellFactory.of(nameOrderRow, LocaleController.getString(R.string.NameOrder), switch (NemoConfig.nameOrder) {
            case 2 -> LocaleController.getString(R.string.LastFirst);
            default -> LocaleController.getString(R.string.FirstLast);
        }).slug("nameOrder"));
        items.add(TextSettingsCellFactory.of(idTypeRow, LocaleController.getString(R.string.IdType), switch (NemoConfig.idType) {
            case NemoConfig.ID_TYPE_HIDDEN -> LocaleController.getString(R.string.IdTypeHidden);
            case NemoConfig.ID_TYPE_BOTAPI -> LocaleController.getString(R.string.IdTypeBOTAPI);
            default -> LocaleController.getString(R.string.IdTypeAPI);
        }).slug("idType"));
        items.add(UItem.asShadow(LocaleController.getString(R.string.IdTypeAbout)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.General)));
        items.add(UItem.asCheck(disabledInstantCameraRow, LocaleController.getString(R.string.DisableInstantCamera)).slug("disabledInstantCamera").setChecked(NemoConfig.disableInstantCamera));
        items.add(UItem.asCheck(askBeforeCallRow, LocaleController.getString(R.string.AskBeforeCalling)).slug("askBeforeCall").setChecked(NemoConfig.askBeforeCall));
        items.add(UItem.asCheck(openArchiveOnPullRow, LocaleController.getString(R.string.OpenArchiveOnPull)).slug("openArchiveOnPull").setChecked(NemoConfig.openArchiveOnPull));
        items.add(UItem.asCheck(alwaysShowDownloadIconRow, LocaleController.getString(R.string.AlwaysShowDownloadIcon)).slug("alwaysShowDownloadIcon").setChecked(NemoConfig.alwaysShowDownloadIcon));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id == ipv6Row) {
            NemoConfig.toggleIPv6();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.preferIPv6);
            }
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                if (UserConfig.getInstance(a).isClientActivated()) {
                    ConnectionsManager.getInstance(a).checkConnection();
                }
            }
        } else if (id == disabledInstantCameraRow) {
            NemoConfig.toggleDisabledInstantCamera();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.disableInstantCamera);
            }
        } else if (id == hideGiftsRow) {
        NemoConfig.toggleHideGifts();
        if (view instanceof TextCheckCell) {
            ((TextCheckCell) view).setChecked(NemoConfig.hideGifts);
        }
        parentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_ONLY_LAST);
        } else if (id == musicViewAlternativeLayoutRow) {
            NemoConfig.toggleMusicViewAlternativeLayout();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.musicViewAlternativeLayout);
            }
            parentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_ONLY_LAST);
        } else if (id == nameOrderRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.FirstLast));
            types.add(1);
            arrayList.add(LocaleController.getString(R.string.LastFirst));
            types.add(2);
            PopupHelper.show(arrayList, LocaleController.getString(R.string.NameOrder), types.indexOf(NemoConfig.nameOrder), getParentActivity(), view, i -> {
                NemoConfig.setNameOrder(types.get(i));
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
                parentLayout.rebuildAllFragmentViews(false, false);
            }, resourcesProvider);
        } else if (id == translationProviderRow) {
            Translator.showTranslationProviderSelector(getParentActivity(), view, param -> {
                item.textValue = getTranslationProvider();
                if (param) {
                    listView.adapter.notifyItemChanged(position, PARTIAL);
                } else {
                    listView.adapter.notifyItemChanged(position, PARTIAL);
                    notifyItemChanged(translationTargetRow, PARTIAL);
                }
            }, resourcesProvider);
        } else if (id == translationTargetRow) {
            Translator.showTranslationTargetSelector(this, view, () -> {
                item.textValue = getTranslationTarget();
                listView.adapter.notifyItemChanged(position, PARTIAL);
                if (Translator.getRestrictedLanguages().size() == 1) {
                    notifyItemChanged(doNotTranslateRow, PARTIAL);
                }
            }, resourcesProvider);
        } else if (id == openArchiveOnPullRow) {
            NemoConfig.toggleOpenArchiveOnPull();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.openArchiveOnPull);
            }
        } else if (id == askBeforeCallRow) {
            NemoConfig.toggleAskBeforeCall();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.askBeforeCall);
            }
        } else if (id == idTypeRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.IdTypeHidden));
            types.add(NemoConfig.ID_TYPE_HIDDEN);
            arrayList.add(LocaleController.getString(R.string.IdTypeAPI));
            types.add(NemoConfig.ID_TYPE_API);
            arrayList.add(LocaleController.getString(R.string.IdTypeBOTAPI));
            types.add(NemoConfig.ID_TYPE_BOTAPI);
            PopupHelper.show(arrayList, LocaleController.getString(R.string.IdType), types.indexOf(NemoConfig.idType), getParentActivity(), view, i -> {
                NemoConfig.setIdType(types.get(i));
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
                parentLayout.rebuildAllFragmentViews(false, false);
            }, resourcesProvider);
        } else if (id == accentAsNotificationColorRow) {
            NemoConfig.toggleAccentAsNotificationColor();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.accentAsNotificationColor);
            }
        } else if (id == useStockNotificationIconRow) {
            NemoConfig.toggleUseStockNotificationIcon();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.useStockNotificationIcon);
            }
        } else if (id == silenceNonContactsRow) {
            NemoConfig.toggleSilenceNonContacts();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.silenceNonContacts);
            }
        } else if (id == translatorTypeRow) {
            int oldType = NemoConfig.transType;
            Translator.showTranslatorTypeSelector(getParentActivity(), view, () -> {
                int newType = NemoConfig.transType;
                item.textValue = getTranslatorType();
                listView.adapter.notifyItemChanged(position, PARTIAL);
                if (oldType != newType) {
                    int count = 4;
                    if (oldType == NemoConfig.TRANS_TYPE_NEMO || newType == NemoConfig.TRANS_TYPE_NEMO) {
                        count++;
                    }
                    if (oldType == NemoConfig.TRANS_TYPE_EXTERNAL) {
                        notifyItemRemoved(translatorExternalAppRow);
                        updateRows();
                        notifyItemRangeInserted(translationProviderRow, count);
                    } else if (newType == NemoConfig.TRANS_TYPE_EXTERNAL) {
                        notifyItemRangeRemoved(translationProviderRow, count);
                        updateRows();
                        notifyItemInserted(translatorExternalAppRow);
                    } else if (oldType == NemoConfig.TRANS_TYPE_NEMO) {
                        notifyItemRemoved(showOriginalRow);
                        updateRows();
                    } else if (newType == NemoConfig.TRANS_TYPE_NEMO) {
                        updateRows();
                        notifyItemInserted(showOriginalRow);
                    }
                }
            }, resourcesProvider);
        } else if (id == doNotTranslateRow) {
            presentFragment(new NemoLanguagesSelectActivity(NemoLanguagesSelectActivity.TYPE_RESTRICTED));
        } else if (id == autoTranslateRow) {
            NemoConfig.toggleAutoTranslate();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.autoTranslate);
            }
        } else if (id == showOriginalRow) {
            NemoConfig.toggleShowOriginal();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.showOriginal);
            }
        } else if (id == translatorExternalAppRow) {
            Translator.showTranslationProviderSelector(getParentActivity(), view, param -> {
                item.textValue = getTranslatorExternalApp();
                listView.adapter.notifyItemChanged(position, PARTIAL);
            }, resourcesProvider);
        } else if (id == alwaysShowDownloadIconRow) {
            NemoConfig.toggleAlwaysShowDownloadIcon();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.alwaysShowDownloadIcon);
            }
        }
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.General);
    }

    @Override
    protected String getKey() {
        return "g";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listView != null) {
            var restrictedLanguageItem = listView.findItemByItemId(doNotTranslateRow);
            if (restrictedLanguageItem != null) {
                restrictedLanguageItem.textValue = getRestrictedLanguages();
                notifyItemChanged(doNotTranslateRow, PARTIAL);
            }
            var translationTargetItem = listView.findItemByItemId(translationTargetRow);
            if (translationTargetItem != null) {
                translationTargetItem.textValue = getTranslationTarget();
                notifyItemChanged(translationTargetRow, PARTIAL);
            }
        }
    }
}
