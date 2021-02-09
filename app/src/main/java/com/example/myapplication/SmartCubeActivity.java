package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import vpos.apipackage.APDU_RESP;
import vpos.apipackage.APDU_SEND;
import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;

public class SmartCubeActivity extends Activity {

	// private EditText ed_sendAPDU = null;
	private Button bt_openIcCard = null;
	private Button bt_close = null;
	private EditText ed_ResponseData = null;
	long mExitTime = 0;
	boolean open_flg = false;
	byte cardsocket = 0x00;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xm_test);
		init_id();
	}

	@Override
	protected void onStart() {
		super.onStart();
		PosApiHelper.getInstance().SysSetPower(1);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PosApiHelper.getInstance().SysSetPower(0);
	}

	public void show(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	private void show_result(int ret) {
		// TODO Auto-generated method stub
		switch (ret) {
		case 0:
			show(SmartCubeActivity.this, "success ");
			break;
		case -1:
			show(SmartCubeActivity.this, "fail");
			break;
		case -2:
			show(SmartCubeActivity.this, "time out");
			break;
		case -3:
			show(SmartCubeActivity.this, "in parameters error");
			break;
		default:
			show(SmartCubeActivity.this, "fail");
			break;

		}
	}

	public void openIcCard(View view) {
		Log.i("123", "IccCheck");
		int ret = PosApiHelper.getInstance().IccCheck(cardsocket);

		byte buf[] = new byte[9];
		PosApiHelper.getInstance().SysGetVersion(buf);
		String out_temp = ByteUtil.bytearrayToHexString(buf, ByteUtil.returnActualLength(buf));
		ed_ResponseData.setText(out_temp);

		if (ret == 0) {
			open_flg = true;
		} else {
			show(SmartCubeActivity.this, "fail: " + ret);
		}

		bt_openIcCard.setBackgroundColor(Color.rgb(0, 0, 255));
	}

	public void activate(View view) {
		if (!open_flg) {
			show(SmartCubeActivity.this, "Please open IcCard");
			return;
		}
		byte[] out_dat = new byte[32];
		int ret = PosApiHelper.getInstance().IccOpen(cardsocket, (byte) 1, out_dat);
		if (ret == 0) {
			String out_temp = ByteUtil.bytearrayToHexString(out_dat,ByteUtil.returnActualLength(out_dat));
			Log.i("123", "ATR: " + out_temp);
			ed_ResponseData.setText(out_temp);
			show(SmartCubeActivity.this, "success: " + ret);
		} else {
			show(SmartCubeActivity.this, "fail: " + ret);
		}
	}

	public void seek(View view) {
		if (!open_flg) {
			show(SmartCubeActivity.this, "Please open IcCard");
			return;
		}
		int ret = 0;
		// int ret = icCard.seek();

		show_result(ret);
	}

	public void exeAPDU1(View view) {
		// 00 a4 04 00 05 49 47 54 4a 4d
		byte cmd[] = new byte[4];
		cmd[0] = 0x00;
		cmd[1] = (byte) 0xa4;
		cmd[2] = 0x04;
		cmd[3] = 0x00;
		byte data[] = new byte[5];
		data[0] = 0x49;
		data[1] = 0x47;
		data[2] = 0x54;
		data[3] = 0x50;
		data[4] = 0x43;

		APDU_SEND s = new APDU_SEND(cmd, (short) 5, data, (short) 0);
		exeAPDU(s.getBytes());
	}

	public void exeAPDU2(View view) {
		// 00 05 00 00
		// 25
		// 02 41 43 4b 5f 36 30 20 20 20 20 20 20 20 20 20
		// 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20
		// 20 cc 67 09 35
		byte cmd[] = new byte[] { 0x00, 0x05, 0x00, 0x00 };
		byte data[] = new byte[] {
				(byte) 0x02, (byte) 0x41, (byte) 0x43, (byte) 0x4b, (byte) 0x5f, (byte) 0x36, (byte) 0x30, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0xcc, (byte) 0x67, (byte) 0x09, (byte) 0x35
		};
		APDU_SEND s = new APDU_SEND(cmd, (short) 0x25, data, (short) 0);
		exeAPDU(s.getBytes());
	}

	public void exeAPDU3(View view) {
		// 00 05 01 00 
		// 80
		// 58 3b a2 49 d5 a5 c5 c4 50 cf 86 91 80 79 e3 b4 
		// 09 2b 79 82 21 f7 e3 e6 ff eb 78 d8 47 45 b0 f4
		// 43 65 11 c4 1f 82 74 91 9a 29 d8 4e 23 49 b4 d2
		// 37 c2 92 c6 67 bb eb 96 d5 96 6c 9f be a2 8b 47
		// 6a 1f bf 5e 85 16 e1 96 07 f9 c9 9c f3 57 46 36
		// 6b f3 af 99 5b 47 0d eb 14 f0 fc 65 15 bf 23 a7
		// 85 8d 80 32 43 3a 3d d7 de 08 06 e7 36 cd e3 f6
		// 2a 93 c4 21 87 11 9f 41 b0 34 f2 4c 0b 86 b8 7d
		byte cmd[] = new byte[] { 0x00, 0x05, 0x01, 0x00 };
		byte data[] = new byte[] {
				(byte) 0x58, (byte) 0x3b, (byte) 0xa2, (byte) 0x49, (byte) 0xd5, (byte) 0xa5, (byte) 0xc5, (byte) 0xc4, (byte) 0x50, (byte) 0xcf, (byte) 0x86, (byte) 0x91, (byte) 0x80, (byte) 0x79, (byte) 0xe3, (byte) 0xb4,
				(byte) 0x09, (byte) 0x2b, (byte) 0x79, (byte) 0x82, (byte) 0x21, (byte) 0xf7, (byte) 0xe3, (byte) 0xe6, (byte) 0xff, (byte) 0xeb, (byte) 0x78, (byte) 0xd8, (byte) 0x47, (byte) 0x45, (byte) 0xb0, (byte) 0xf4,
				(byte) 0x43, (byte) 0x65, (byte) 0x11, (byte) 0xc4, (byte) 0x1f, (byte) 0x82, (byte) 0x74, (byte) 0x91, (byte) 0x9a, (byte) 0x29, (byte) 0xd8, (byte) 0x4e, (byte) 0x23, (byte) 0x49, (byte) 0xb4, (byte) 0xd2,
				(byte) 0x37, (byte) 0xc2, (byte) 0x92, (byte) 0xc6, (byte) 0x67, (byte) 0xbb, (byte) 0xeb, (byte) 0x96, (byte) 0xd5, (byte) 0x96, (byte) 0x6c, (byte) 0x9f, (byte) 0xbe, (byte) 0xa2, (byte) 0x8b, (byte) 0x47,
				(byte) 0x6a, (byte) 0x1f, (byte) 0xbf, (byte) 0x5e, (byte) 0x85, (byte) 0x16, (byte) 0xe1, (byte) 0x96, (byte) 0x07, (byte) 0xf9, (byte) 0xc9, (byte) 0x9c, (byte) 0xf3, (byte) 0x57, (byte) 0x46, (byte) 0x36,
				(byte) 0x6b, (byte) 0xf3, (byte) 0xaf, (byte) 0x99, (byte) 0x5b, (byte) 0x47, (byte) 0x0d, (byte) 0xeb, (byte) 0x14, (byte) 0xf0, (byte) 0xfc, (byte) 0x65, (byte) 0x15, (byte) 0xbf, (byte) 0x23, (byte) 0xa7,
				(byte) 0x85, (byte) 0x8d, (byte) 0x80, (byte) 0x32, (byte) 0x43, (byte) 0x3a, (byte) 0x3d, (byte) 0xd7, (byte) 0xde, (byte) 0x08, (byte) 0x06, (byte) 0xe7, (byte) 0x36, (byte) 0xcd, (byte) 0xe3, (byte) 0xf6,
				(byte) 0x2a, (byte) 0x93, (byte) 0xc4, (byte) 0x21, (byte) 0x87, (byte) 0x11, (byte) 0x9f, (byte) 0x41, (byte) 0xb0, (byte) 0x34, (byte) 0xf2, (byte) 0x4c, (byte) 0x0b, (byte) 0x86, (byte) 0xb8, (byte) 0x7d
		};
		APDU_SEND s = new APDU_SEND(cmd, (short) data.length, data, (short) 0);
		exeAPDU(s.getBytes());
	}

	private void exeAPDU(byte[] in_dat) {

		Log.i("123", "send: " + ByteUtil.bytearrayToHexString(in_dat, ByteUtil.returnActualLength(in_dat)));

		int ret;
		byte[] out_dat = new byte[516];
		ret = PosApiHelper.getInstance().IccCommand(cardsocket, in_dat, out_dat);

		if (ret == 0) {

			Log.i("123", "receive: " + ByteUtil.bytearrayToHexString(out_dat,  ByteUtil.returnActualLength(in_dat)));

			APDU_RESP resp = new APDU_RESP(out_dat);

			byte[] res = new byte[resp.getLenOut()];
			System.arraycopy(resp.getDataOut(), 0, res, 0, res.length);

			StringBuilder sb = new StringBuilder(ByteUtil.bytearrayToHexString(res, ByteUtil.returnActualLength(res)));
			sb.append(' ').append(Integer.toHexString(resp.getSWA() & 0xff)).append(' ')
					.append(Integer.toHexString(resp.getSWB() & 0xff));

			ed_ResponseData.setText(sb.toString());
			show(SmartCubeActivity.this, "read length:" + resp.getLenOut());
		} else {
			Log.i("123", "fail: " + ret);
			show(SmartCubeActivity.this, "fail: " + ret);
		}
	}

	public void move(View view) {
		if (!open_flg) {
			show(SmartCubeActivity.this, "Please open IcCard");
			return;
		}
		Log.i("123", "move");
		int ret = PosApiHelper.getInstance().IccClose(cardsocket);
		show_result(ret);
	}

	public void close(View view) {
		Log.i("123", "close");
		int ret = 0;
		if (ret == 0) {
			open_flg = false;
		}
		show_result(ret);
		bt_close.setBackgroundColor(Color.rgb(0, 0, 255));
		System.exit(0);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "Once press again,exit program", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				// icCard.close();
				// icCard = null;
				System.exit(0);
			}

			return true;

		}

		return super.onKeyDown(keyCode, event);
	}

	public void init_id() {

		// ed_sendAPDU = (EditText)findViewById(R.id.ed_sendAPDU);
		bt_openIcCard = (Button) findViewById(R.id.bt_openIcCard);
		bt_close = (Button) findViewById(R.id.bt_close);
		ed_ResponseData = (EditText) findViewById(R.id.ed_ResponseData);
	}

}
