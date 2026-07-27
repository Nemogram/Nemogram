package org.nemogram.messenger.pgp.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.View;

import org.nemogram.messenger.helpers.PopupHelper;
import org.nemogram.messenger.pgp.PgpConfig;
import org.nemogram.messenger.pgp.PgpServiceManager;
import org.nemogram.messenger.settings.BaseNemoSettingsActivity;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;
import java.util.List;

public class PgpSettingsActivity extends BaseNemoSettingsActivity {

    private static final String OPENKEYCHAIN_PACKAGE = "org.sufficientlysecure.keychain";
    private static final String OPENKEYCHAIN_FDROID_URL = "https://f-droid.org/packages/org.sufficientlysecure.keychain/";

    private final int providerRow = rowId++;
    private final int myKeyRow = rowId++;
    private final int resetRow = rowId++;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.PgpSettings);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.PgpSettings)));
        items.add(TextSettingsCellFactory.of(providerRow, LocaleController.getString(R.string.PgpProvider), providerLabel()));
        UItem myKeyItem = TextSettingsCellFactory.of(myKeyRow, LocaleController.getString(R.string.PgpMyKey), myKeyLabel());
        myKeyItem.enabled = PgpConfig.isProviderConfigured();
        items.add(myKeyItem);
        items.add(UItem.asShadow(LocaleController.getString(R.string.PgpSettingsAbout)));

        if (PgpConfig.isProviderConfigured()) {
            UItem resetItem = TextSettingsCellFactory.of(resetRow, LocaleController.getString(R.string.PgpResetAll));
            resetItem.red = true;
            items.add(resetItem);
            items.add(UItem.asShadow(LocaleController.getString(R.string.PgpResetAllAbout)));
        }
    }

    private String providerLabel() {
        String pkg = PgpConfig.getProviderPackage();
        if (pkg == null) {
            return LocaleController.getString(R.string.PgpProviderNotSelected);
        }
        return appLabel(pkg);
    }

    private String myKeyLabel() {
        if (!PgpConfig.isProviderConfigured() || PgpConfig.getMyKeyId() == 0) {
            return LocaleController.getString(R.string.PgpMyKeyNotSelected);
        }
        String userId = PgpConfig.getMyUserId();
        return userId != null ? userId : Long.toHexString(PgpConfig.getMyKeyId());
    }

    private String appLabel(String pkg) {
        try {
            PackageManager pm = ApplicationLoader.applicationContext.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id == providerRow) {
            chooseProvider(view);
        } else if (id == myKeyRow) {
            chooseMyKey();
        } else if (id == resetRow) {
            confirmResetAll();
        }
    }

    private void confirmResetAll() {
        var builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
        builder.setTitle(LocaleController.getString(R.string.PgpResetAll));
        builder.setMessage(LocaleController.getString(R.string.PgpResetAllConfirm));
        builder.setPositiveButton(LocaleController.getString(R.string.PgpResetAll), (dialog, which) -> {
            PgpConfig.resetAll();
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        var dialog = builder.create();
        showDialog(dialog);
        var button = (android.widget.TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_text_RedRegular, resourcesProvider));
        }
    }

    private void chooseProvider(View anchor) {
        List<String> providers = PgpServiceManager.getInstance().findAvailableProviders();
        // app might be installed but not yet resolvable via queryIntentServices on some roms
        if (!providers.contains(OPENKEYCHAIN_PACKAGE) && PgpServiceManager.getInstance().isProviderInstalled(OPENKEYCHAIN_PACKAGE)) {
            providers.add(0, OPENKEYCHAIN_PACKAGE);
        }
        if (providers.isEmpty()) {
            var builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.PgpProvider));
            builder.setMessage(LocaleController.getString(R.string.PgpProviderNoneFound));
            builder.setPositiveButton(LocaleController.getString(R.string.PgpProviderInstallOpenKeychain), (dialog, which) ->
                    Browser.openUrl(getParentActivity(), OPENKEYCHAIN_FDROID_URL));
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            showDialog(builder.create());
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        for (String pkg : providers) {
            labels.add(appLabel(pkg));
        }
        int selected = providers.indexOf(PgpConfig.getProviderPackage());
        PopupHelper.show(labels, LocaleController.getString(R.string.PgpProviderChoose), selected, getParentActivity(), anchor, i -> {
            PgpConfig.setProviderPackage(providers.get(i));
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        }, resourcesProvider);
    }

    private void chooseMyKey() {
        if (!PgpConfig.isProviderConfigured()) {
            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.PgpSelectProviderFirst)).show();
            return;
        }
        PgpServiceManager.getInstance().pickMyKey(new PgpServiceManager.PgpResultCallback<Long>() {
            @Override
            public void onSuccess(Long keyId) {
                AndroidUtilitiesRunOnUI(() -> {
                    if (listView != null && listView.adapter != null) {
                        listView.adapter.update(true);
                    }
                });
            }

            @Override
            public void userInteractionRequired(android.app.PendingIntent pendingIntent) {
                AndroidUtilitiesRunOnUI(() -> {
                    if (getParentActivity() != null) {
                        PgpServiceManager.startUserInteraction(getParentActivity(), pendingIntent);
                    }
                });
            }

            @Override
            public void onError(String message) {
                AndroidUtilitiesRunOnUI(() -> BulletinFactory.of(PgpSettingsActivity.this)
                        .createErrorBulletin(LocaleController.formatString(R.string.PgpError, message)).show());
            }
        });
    }

    private void AndroidUtilitiesRunOnUI(Runnable r) {
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r);
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        if (PgpServiceManager.getInstance().onActivityResult(requestCode, resultCode, data)) {
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        }
    }
}
