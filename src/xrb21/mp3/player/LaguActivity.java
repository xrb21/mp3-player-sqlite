package xrb21.mp3.player;

import java.util.ArrayList;

import xrb21.mp3.player.database.DBAdapter;
import xrb21.mp3.player.models.LaguPramuka;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class LaguActivity extends Activity {
	private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.3F);
	private ArrayList<LaguPramuka> data;
	private ListView lvList;

	private Button buttonPlayStop;
	private MediaPlayer mediaPlayer;
	private SeekBar seekBar;

	private final Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lagu);

		setupView();
		ambilData();

	}

	private void setupView() {
		lvList = (ListView) findViewById(R.id.lvSejarah);
		lvList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View v, int posisi,
					long id) {
				v.startAnimation(buttonClick);
				LaguPramuka x = data.get(posisi);
				// RbHelper.pesan(getBaseContext(), "Lagu : " + x.getIsi());
				getInfoDetail(x);

			}
		});

	}

	public int getImage(String name) {
		String x = name;
		System.out.println("nama : " + x);
		try {
			if (name.contains(".")) {
				System.out.println("ada titik");
				x = removeExtensi(name);
				System.out.println("hasil split " + x);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return getResources().getIdentifier(x, "drawable", getPackageName());
	}

	private String removeExtensi(String name) {
		int p = name.length();
		String hasil = "";
		try {
			hasil = name.substring(0, p - 4);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return hasil;
	}

	protected void getInfoDetail(final LaguPramuka kat) {
		LayoutInflater inflater = LayoutInflater.from(LaguActivity.this);
		View dialogview = inflater.inflate(R.layout.custom_detail_lagu, null);

		
		buttonPlayStop = (Button) dialogview.findViewById(R.id.ButtonPlayStop);
		buttonPlayStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonClick();
			}
		});

		int d = getImage(kat.getIsi());
		try {
			mediaPlayer = MediaPlayer.create(this, d);

			seekBar = (SeekBar) dialogview.findViewById(R.id.SeekBar01);
			seekBar.setMax(mediaPlayer.getDuration());
			seekBar.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					seekChange(v);
					return false;
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(),
					"Lagu " + kat.getIsi() + " tidak tersedia.",
					Toast.LENGTH_LONG).show();
			return;
		}

		AlertDialog.Builder builderx = new AlertDialog.Builder(this);
		builderx.setView(dialogview);

		builderx.setTitle(kat.getTitle());
		builderx.setCancelable(false);
		builderx.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mediaPlayer.stop();
				dialog.cancel();
			}
		});
		builderx.show();

	}

	public void startPlayProgressUpdater() {
		seekBar.setProgress(mediaPlayer.getCurrentPosition());

		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
				public void run() {
					startPlayProgressUpdater();
				}
			};
			handler.postDelayed(notification, 1000);
		} else {
			mediaPlayer.pause();
			buttonPlayStop.setText("Play");
			seekBar.setProgress(0);
		}
	}

	// This is event handler thumb moving event
	private void seekChange(View v) {
		if (mediaPlayer.isPlaying()) {
			SeekBar sb = (SeekBar) v;
			mediaPlayer.seekTo(sb.getProgress());
		}
	}

	// This is event handler for buttonClick event
	private void buttonClick() {
		if (buttonPlayStop.getText().equals("Play")) {
			buttonPlayStop.setText("Pause");
			try {
				mediaPlayer.start();
				startPlayProgressUpdater();
			} catch (IllegalStateException e) {
				mediaPlayer.pause();
			}
		} else {
			buttonPlayStop.setText("Play");
			mediaPlayer.pause();
		}
	}

	private void ambilData() {
		data = new ArrayList<LaguPramuka>();
		DBAdapter db = new DBAdapter(getBaseContext());
		db.openDataBase();

		Cursor cur = db.selectData(
				"SELECT * FROM lagu_pramuka ORDER BY id_lagupramuka ASC", null);

		Log.i("jumlah history", String.valueOf(cur.getCount()));
		cur.moveToFirst();
		if (cur.getCount() > 0) {
			while (cur.isAfterLast() == false) {

				LaguPramuka x = new LaguPramuka();
				x.setId((cur.getInt(cur.getColumnIndexOrThrow("id_lagupramuka"))));
				x.setTitle((cur.getString(cur
						.getColumnIndexOrThrow("lagupramuka_nama"))));
				x.setIsi((cur.getString(cur
						.getColumnIndexOrThrow("lagupramuka_lagu"))));

				data.add(x);
				cur.moveToNext();
			}
			db.close();
			// masukkan kedalam listview
			CustomAdapter adapter = new CustomAdapter(getBaseContext(), data);
			lvList.setAdapter(adapter);
		} else {
			// masukkan kedalam listview
			CustomAdapter adapter = new CustomAdapter(getBaseContext(), data);
			lvList.setAdapter(adapter);
			TextView tv = new TextView(this);
			tv.setText("Data tidak Ada");
			lvList.setEmptyView(tv);
		}

	}

	// subclass untuk custom adapter pada listview
	private class CustomAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<LaguPramuka> dataz;
		private LayoutInflater inflater = null;

		public CustomAdapter(Context c, ArrayList<LaguPramuka> data) {
			context = c;
			dataz = data;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return dataz.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View view, ViewGroup parent) {
			View vi = view;
			if (view == null)
				vi = inflater.inflate(R.layout.custom_adapter_listview, null);

			TextView title = (TextView) vi
					.findViewById(R.id.tvCustomAdapterLvTitle);

			LaguPramuka x = dataz.get(position);

			title.setText(x.getTitle());

			return vi;
		}

	}

}
