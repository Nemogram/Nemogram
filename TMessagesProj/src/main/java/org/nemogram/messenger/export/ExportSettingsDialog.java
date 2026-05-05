package org.nemogram.messenger.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.TextRadioCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;

public class ExportSettingsDialog extends BottomSheet {

    private static final String[] MSG_LABELS = {"No limit", "500", "1 000", "5 000", "10 000"};
    private static final String[] MEDIA_LABELS = {"No limit", "50 MB", "200 MB", "500 MB", "1 GB"};
    private final ExportSettings settings = new ExportSettings();
    private final long dialogId;
    private TextView progressText;
    private LinearLayout progressLayout;
    private TextSettingsCell msgLimitCell;
    private TextSettingsCell mediaLimitCell;
    private TextRadioCell radioHtml;
    private TextRadioCell radioJson;
    private TextView startBtn;

    public ExportSettingsDialog(Context context, long dialogId, Theme.ResourcesProvider resourcesProvider) {
        super(context, false, resourcesProvider);
        this.dialogId = dialogId;
        buildUI(context);
    }

    private void buildUI(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setText(LocaleController.getString(R.string.ExportChat));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        title.setPadding(AndroidUtilities.dp(23), AndroidUtilities.dp(16), AndroidUtilities.dp(23), AndroidUtilities.dp(8));
        root.addView(title, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        root.addView(makeDivider(context));

        root.addView(makeSectionHeader(context, LocaleController.getString(R.string.ExportChatInclude)));

        root.addView(makeCheck(context, LocaleController.getString(R.string.ExportChatPhotos),
                settings.includePhotos, v -> settings.includePhotos = !settings.includePhotos, true));
        root.addView(makeCheck(context, LocaleController.getString(R.string.ExportChatVideos),
                settings.includeVideos, v -> settings.includeVideos = !settings.includeVideos, true));
        root.addView(makeCheck(context, LocaleController.getString(R.string.ExportChatFiles),
                settings.includeFiles, v -> settings.includeFiles = !settings.includeFiles, true));
        root.addView(makeCheck(context, LocaleController.getString(R.string.ExportChatVoice),
                settings.includeVoice, v -> settings.includeVoice = !settings.includeVoice, true));
        root.addView(makeCheck(context, LocaleController.getString(R.string.ExportChatMusic),
                settings.includeMusic, v -> settings.includeMusic = !settings.includeMusic, false));

        root.addView(makeDivider(context));

        root.addView(makeSectionHeader(context, LocaleController.getString(R.string.ExportChatFormat)));

        radioHtml = new TextRadioCell(context, resourcesProvider);
        radioHtml.setTextAndCheck(LocaleController.getString(R.string.ExportChatFormatHtml), true, true);
        radioHtml.setOnClickListener(v -> {
            settings.format = ExportSettings.Format.HTML;
            radioHtml.setChecked(true);
            radioJson.setChecked(false);
        });
        root.addView(radioHtml);

        radioJson = new TextRadioCell(context, resourcesProvider);
        radioJson.setTextAndCheck(LocaleController.getString(R.string.ExportChatFormatJson), false, false);
        radioJson.setOnClickListener(v -> {
            settings.format = ExportSettings.Format.JSON;
            radioJson.setChecked(true);
            radioHtml.setChecked(false);
        });
        root.addView(radioJson);

        root.addView(makeDivider(context));

        root.addView(makeSectionHeader(context, LocaleController.getString(R.string.ExportChatMessageLimit)));

        msgLimitCell = new TextSettingsCell(context, resourcesProvider);
        msgLimitCell.setTextAndValue(
                LocaleController.getString(R.string.ExportChatMessageLimit),
                MSG_LABELS[0], false, false);
        msgLimitCell.setOnClickListener(v -> showPicker(
                LocaleController.getString(R.string.ExportChatMessageLimit),
                MSG_LABELS, getSelectedMsgIndex(),
                idx -> {
                    settings.maxMessages = ExportSettings.MESSAGE_LIMITS[idx];
                    msgLimitCell.setTextAndValue(
                            LocaleController.getString(R.string.ExportChatMessageLimit),
                            MSG_LABELS[idx], true, false);
                }));
        root.addView(msgLimitCell);

        mediaLimitCell = new TextSettingsCell(context, resourcesProvider);
        mediaLimitCell.setTextAndValue(
                LocaleController.getString(R.string.ExportChatMediaLimit),
                MEDIA_LABELS[0], false, false);
        mediaLimitCell.setOnClickListener(v -> showPicker(
                LocaleController.getString(R.string.ExportChatMediaLimit),
                MEDIA_LABELS, getSelectedMediaIndex(),
                idx -> {
                    settings.maxMediaBytes = ExportSettings.MEDIA_LIMITS[idx];
                    mediaLimitCell.setTextAndValue(
                            LocaleController.getString(R.string.ExportChatMediaLimit),
                            MEDIA_LABELS[idx], true, false);
                }));
        root.addView(mediaLimitCell);

        root.addView(makeDivider(context));

        progressLayout = new LinearLayout(context);
        progressLayout.setOrientation(LinearLayout.VERTICAL);
        progressLayout.setVisibility(View.GONE);
        progressLayout.setPadding(AndroidUtilities.dp(23), AndroidUtilities.dp(8),
                AndroidUtilities.dp(23), AndroidUtilities.dp(4));

        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setIndeterminate(true);
        progressLayout.addView(bar,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 4, 0, 0, 0, 6));

        progressText = new TextView(context);
        progressText.setTextColor(Theme.getColor(
                Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        progressText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        progressLayout.addView(progressText);
        root.addView(progressLayout,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        startBtn = new TextView(context);
        startBtn.setText(LocaleController.getString(R.string.ExportChatStart));
        startBtn.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
        startBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        startBtn.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        startBtn.setGravity(android.view.Gravity.CENTER);
        startBtn.setBackground(Theme.createSimpleSelectorRoundRectDrawable(
                AndroidUtilities.dp(24),
                Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider),
                Theme.getColor(Theme.key_featuredStickers_addButtonPressed, resourcesProvider)
        ));
        startBtn.setOnClickListener(v -> startExport());
        root.addView(startBtn, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, 48,
                android.view.Gravity.BOTTOM,
                16, 12, 16, 16
        ));

        setCustomView(root);
    }

    private TextCheckbox2Cell makeCheck(Context context, String label,
                                        boolean initial, View.OnClickListener toggle,
                                        boolean divider) {
        TextCheckbox2Cell cell = new TextCheckbox2Cell(context, resourcesProvider);
        cell.setTextAndCheck(label, initial, divider);
        cell.setOnClickListener(v -> {
            toggle.onClick(v);
            cell.setChecked(!cell.isChecked());
        });
        return cell;
    }

    private TextView makeSectionHeader(Context context, String text) {
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        label.setPadding(AndroidUtilities.dp(23), AndroidUtilities.dp(12),
                AndroidUtilities.dp(23), AndroidUtilities.dp(4));
        return label;
    }

    private View makeDivider(Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(Theme.getColor(Theme.key_divider, resourcesProvider));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divider.setLayoutParams(lp);
        return divider;
    }

    private void showPicker(String title, String[] labels, int currentIdx, PickerCallback cb) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), resourcesProvider);
        builder.setTitle(title);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            RadioColorCell cell = new RadioColorCell(getContext(), resourcesProvider);
            cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
            cell.setTextAndValue(labels[i], idx == currentIdx);
            cell.setOnClickListener(v -> {
                cb.onPicked(idx);
                builder.getDismissRunnable().run();
            });
            ll.addView(cell);
        }

        builder.setView(ll);
        builder.show();
    }

    private int getSelectedMsgIndex() {
        for (int i = 0; i < ExportSettings.MESSAGE_LIMITS.length; i++) {
            if (ExportSettings.MESSAGE_LIMITS[i] == settings.maxMessages) return i;
        }
        return 0;
    }

    private int getSelectedMediaIndex() {
        for (int i = 0; i < ExportSettings.MEDIA_LIMITS.length; i++) {
            if (ExportSettings.MEDIA_LIMITS[i] == settings.maxMediaBytes) return i;
        }
        return 0;
    }

    private void startExport() {
        progressLayout.setVisibility(View.VISIBLE);
        progressText.setText(LocaleController.getString(R.string.ExportChatPreparing));
        startBtn.setEnabled(false);
        startBtn.setAlpha(0.5f);

        ChatExportManager.getInstance().startExport(dialogId, settings,
                new ChatExportManager.ExportListener() {
                    @Override
                    public void onProgress(int count) {
                        progressText.setText(LocaleController.formatString(
                                R.string.ExportChatProgress, count));
                    }

                    @Override
                    public void onDone(File outputFile) {
                        dismiss();
                        shareFile(outputFile);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressLayout.setVisibility(View.GONE);
                        startBtn.setEnabled(true);
                        startBtn.setAlpha(1f);
                        Toast.makeText(getContext(),
                                LocaleController.formatString(R.string.ExportChatError,
                                        e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(
                ApplicationLoader.applicationContext,
                ApplicationLoader.applicationContext.getPackageName() + ".provider",
                file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getContext().startActivity(Intent.createChooser(intent,
                LocaleController.getString(R.string.ExportChat)));
    }

    private interface PickerCallback {
        void onPicked(int index);
    }
}