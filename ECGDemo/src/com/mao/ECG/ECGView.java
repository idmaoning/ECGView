package com.mao.ECG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

/**
 * @author maoning 2016/4/12
 */
public class ECGView extends View {
	private final static String X_KEY = "Xpos";
	private final static String Y_KEY = "Ypos";
	private final static int LOW_ALARM = 0;
	private final static int HIGH_ALARM = 1;
	private int _backLineColor;
	private int _titleColor;
	private int _pointerLineColor;
	private int _titleSize;
	private int _XYTextSize;
	// 屏幕上的数量
	private int _PointMaxAmount;
	private float _XUnitLength;
	// 当前加入点	
	private int _CurP = 0;
	private int _RemovedPointNum = 0;
	private int _EveryNPointBold = 1;
	// 是否是第第一次加载背景
	private Boolean _isfristDrawBackGround = true;
	// 上下左右缩进
	private int _LeftIndent = 100;
	private int _RightIndent = 100;
	private int _BottomIndent = 100;
	private int _TopIndent = 100;
	private float _CurX = _LeftIndent + 4;
	private float _CurY = _TopIndent;
	// 设置每_EveryNPointRefresh个点刷新电图
	private int _EveryNPointRefresh = 1;
	private float _MaxYNumber;
	private int _Height;
	private int _Width;
	private float _EffectiveHeight = 1;// 有效高度
	private float _EffectiveWidth = 1;// 有效宽度
	private float _EveryOneValue = 1;// 每个格子的�?
	private int _LatticeWidth = 1;
	private List<Map<String, Float>> _ListPoint = new ArrayList<Map<String, Float>>();
	private List<Float> _ListVLine = new ArrayList<Float>();
	private List<Float> _ListHLine = new ArrayList<Float>();
	private Paint _PaintLine;
	private Paint _PaintDataLine;
	private TextPaint _TitleTextPaint;
	private TextPaint _XYTextPaint;
	private String title = "心电图";
	private int _YSize;
	private int _XSize;
	private Context _context;
	private String _lowAlarmMsg;// 低报警消�?
	private String _highAlarmMsg;// 高报警消�?
	private int _maxAlarmNumber;// �?��临界�?
	private int _minAlarmNumber;// �?��临界�?
	private boolean isSetAlarmFlag = false;
	private Handler _handler;

	public ECGView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		_context = context;
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.elg);
		_backLineColor = typedArray.getColor(R.styleable.elg_BackLineColor,
				Color.GREEN);
		_titleColor = typedArray
				.getColor(R.styleable.elg_TitleColor, Color.RED);
		_pointerLineColor = typedArray.getColor(
				R.styleable.elg_PointerLineColor, Color.WHITE);
		_titleSize = typedArray.getDimensionPixelSize(
				R.styleable.elg_TitleSize, 30);
		_XYTextSize = typedArray.getDimensionPixelSize(
				R.styleable.elg_XYTextSize, 20);
		typedArray.recycle();
		initView();
	}

	public ECGView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_context = context;
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.elg);
		_backLineColor = typedArray.getColor(R.styleable.elg_BackLineColor,
				Color.GREEN);
		_titleColor = typedArray
				.getColor(R.styleable.elg_TitleColor, Color.RED);
		_pointerLineColor = typedArray.getColor(
				R.styleable.elg_PointerLineColor, Color.WHITE);
		_titleSize = typedArray.getDimensionPixelSize(
				R.styleable.elg_TitleSize, 30);
		_XYTextSize = typedArray.getDimensionPixelSize(
				R.styleable.elg_XYTextSize, 20);
		typedArray.recycle();
		initView();
	}

	public ECGView(Context context) {
		this(context, null);
		_context = context;
		initView();
	}

	private void initView() {
		_handler = new Handler(Looper.getMainLooper());
		_PaintLine = new Paint();
		_PaintLine.setStrokeWidth(2.5f);
		_PaintLine.setColor(_pointerLineColor);
		_PaintLine.setAntiAlias(true);
		_PaintDataLine = new Paint();
		_PaintDataLine.setColor(_backLineColor);
		_PaintDataLine.setAntiAlias(true);
		_PaintDataLine.setStrokeWidth(10);
		_XYTextPaint = new TextPaint();
		_XYTextPaint.setColor(_titleColor);
		_XYTextPaint.setTextSize(_XYTextSize);
		_TitleTextPaint = new TextPaint();
		_TitleTextPaint.setColor(_titleColor);
		_TitleTextPaint.setTextSize(_titleSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (_isfristDrawBackGround) {
			_Height = getHeight();
			_Width = getWidth();
		}
		_EffectiveHeight = _Height - _TopIndent - _BottomIndent;
		_EffectiveWidth = _Width - _RightIndent - _LeftIndent;
		_XUnitLength = (_EffectiveWidth) / (_PointMaxAmount - 1);// 两条线之间的间距等于宽减出左右缩进除以点�?1
		drawBackground(canvas);
		drawWave(canvas);
	}

	// 画背景图以及格子
	public void drawBackground(Canvas canvas) {
		if (_isfristDrawBackGround) {
			_YSize = (int) (_MaxYNumber / _EveryOneValue);// 垂直格子数量
			_LatticeWidth = (int) (_EffectiveHeight / _YSize);
			_XSize = (_Width - _RightIndent - _LeftIndent) / _LatticeWidth;// 水平格子数量
			float curX = 0;
			if (_EveryNPointBold > _YSize || _EveryNPointBold > _XSize) {
				_EveryNPointBold = Math.min(_YSize, _XSize) / 2 + 1;
			}
			for (int i = 0; i < _XSize; i++) {
				_ListVLine.add(curX);
				curX += _LatticeWidth;
			}
			float curY = 0;
			for (int j = 0; j < _YSize; j++) {
				_ListHLine.add(curY);
				curY += _LatticeWidth;
			}
			_isfristDrawBackGround = false;
		}
		_PaintDataLine.setStrokeWidth(1);
		int sText = 5;
		for (int i = 0; i < _ListVLine.size(); i++) {
			sText = 5 * i;
			canvas.drawText(sText + "", _ListVLine.get(i) + _TopIndent, _Height
					- _TopIndent + _XYTextSize, _XYTextPaint);
			if (i == 0) {
				_PaintDataLine.setStrokeWidth(8);
				canvas.drawLine(_ListVLine.get(i) + _LeftIndent,
						0 + _TopIndent, _ListVLine.get(i) + _LeftIndent,
						_Height - _BottomIndent, _PaintDataLine);
				_PaintDataLine.setStrokeWidth(1);
			} else {
				if (i % _EveryNPointBold == 0) {
					_PaintDataLine.setStrokeWidth(3);
					canvas.drawLine(_ListVLine.get(i) + _LeftIndent,
							0 + _TopIndent, _ListVLine.get(i) + _LeftIndent,
							_Height - _BottomIndent, _PaintDataLine);
					_PaintDataLine.setStrokeWidth(1);
				} else {
					canvas.drawLine(_ListVLine.get(i) + _LeftIndent,
							0 + _TopIndent, _ListVLine.get(i) + _LeftIndent,
							_Height - _BottomIndent, _PaintDataLine);
				}
			}
		}
		_PaintDataLine.setStrokeWidth(8);
		canvas.drawLine(0 + _LeftIndent, _Height - _TopIndent, _Width
				- _RightIndent, _Height - _BottomIndent, _PaintDataLine);
		_PaintDataLine.setStrokeWidth(1);
		String sYText = "";
		for (int i = 0; i < _ListHLine.size(); i++) {
			if (i == 0) {
				sYText = (int) _EveryOneValue * (_YSize - i) + "";
				canvas.drawText(sYText, _LeftIndent - _XYTextSize * 3,
						_ListHLine.get(i) + _TopIndent, _XYTextPaint);
				_PaintDataLine.setStrokeWidth(8);
				canvas.drawLine(0 + _LeftIndent,
						_ListHLine.get(i) + _TopIndent, _Width - _RightIndent,
						_ListHLine.get(i) + _BottomIndent, _PaintDataLine);
				_PaintDataLine.setStrokeWidth(1);
			} else {
				if (i % _EveryNPointBold == 0) {
					sYText = (int) _EveryOneValue * (_YSize - i) + "";
					canvas.drawText(sYText, _LeftIndent - _XYTextSize * 3,
							_ListHLine.get(i) + _TopIndent, _XYTextPaint);
					_PaintDataLine.setStrokeWidth(3);
					canvas.drawLine(0 + _LeftIndent, _ListHLine.get(i)
							+ _TopIndent, _Width - _RightIndent,
							_ListHLine.get(i) + _BottomIndent, _PaintDataLine);
					_PaintDataLine.setStrokeWidth(1);
				} else {
					canvas.drawLine(0 + _LeftIndent, _ListHLine.get(i)
							+ _TopIndent, _Width - _RightIndent,
							_ListHLine.get(i) + _BottomIndent, _PaintDataLine);
				}
			}
		}
		canvas.drawText(title, _Width / 2 - 100, _TopIndent / 2,
				_TitleTextPaint);
		_PaintDataLine.setStrokeWidth(8);
		canvas.drawLine(_Width - _RightIndent, 0 + _TopIndent, _Width
				- _RightIndent, _Height - _BottomIndent, _PaintDataLine);
	}

	// 画点
	public void drawWave(Canvas canvas) {
		for (int index = 0; index < _ListPoint.size(); index++) {
			if (_ListPoint.size() == _PointMaxAmount
					&& (index >= _CurP && index < _CurP + _RemovedPointNum)) {
				continue;
			}
			if (index > 0) {
				if (_ListPoint.get(index).get(Y_KEY) < 0
						|| _ListPoint.get(index).get(Y_KEY) < _TopIndent) {
					continue;
				}
				canvas.drawLine(_ListPoint.get(index - 1).get(X_KEY),
						_ListPoint.get(index - 1).get(Y_KEY),
						_ListPoint.get(index).get(X_KEY), _ListPoint.get(index)
								.get(Y_KEY), _PaintLine);
				canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
						Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			}
		}
	}

	// 设置 心电图的�?
	public void setLinePoint(float curY) {
		Map<String, Float> temp = new HashMap<String, Float>();
		temp.put(X_KEY, _CurX);
		_CurX += _XUnitLength;
		// 计算y真实�?���?
		float number = curY / _EveryOneValue;// 这个数应该占的格子数
		if (_Height != 0) {
			_CurY = _Height - (_BottomIndent + number * _LatticeWidth);
		}
		if (_CurY < _TopIndent) {
			_CurY = _TopIndent + 10;
		}
		temp.put(Y_KEY, _CurY);
		// 判断当前点是否大于最大点�?
		if (_CurP < _PointMaxAmount) {
			try {
				if (_ListPoint.size() == _PointMaxAmount
						&& _ListPoint.get(_CurP) != null) {
					_ListPoint.remove(_CurP);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			_ListPoint.add(_CurP, temp);
			_CurP++;
		} else {
			_CurP = 0;
			_CurX = _RightIndent;
		}
		if (_CurP % _EveryNPointRefresh == 0) {
			invalidate();
		}
		if (isSetAlarmFlag) {
			if (!(curY > _minAlarmNumber && curY < _maxAlarmNumber)) {
				_handler.post(new alarmThread(
						curY < _minAlarmNumber ? LOW_ALARM : HIGH_ALARM));
			}

		}
	}

	public void setRemovedPointNum(int removedPointNum) {
		_RemovedPointNum = removedPointNum;
	}

	// 设置每N个点刷新�?��
	public void setEveryNPointRefresh(int num) {
		_EveryNPointRefresh = num;
	}

	public float getCurrentPointX() {
		return _CurX;
	}

	public float getCurrentPointY() {
		return _CurY;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	// 设置�?��屏幕有多少个�?
	public void setMaxPointAmount(int i) {
		_PointMaxAmount = i;
	}

	// 设置几个格子画一条粗�?
	public void setEveryNPoint(int everyNPointBold) {
		if (everyNPointBold < 0) {
			return;
		}
		_EveryNPointBold = everyNPointBold;
	}

	// 设置Y轴最大�?
	public void setMaxYNumber(float maxYNumber) {
		this._MaxYNumber = maxYNumber;
	}

	// 设置心电图标�?
	public void setTitle(String title) {
		this.title = title;
	}

	// 设置格子的单�?
	public void setEffticeValue(int value) {
		_EveryOneValue = value;
	}

	public void setAlarmMessage(int maxAlarmNumber, int minAlarmNumber,
			String lowAlarmMsg, String highAlarmMsg) {
		isSetAlarmFlag = true;
		_maxAlarmNumber = maxAlarmNumber;
		_minAlarmNumber = minAlarmNumber;
		_lowAlarmMsg = lowAlarmMsg;
		_highAlarmMsg = highAlarmMsg;
	}

	class alarmThread implements Runnable {
		int _flag;

		alarmThread(int flag) {
			_flag = flag;
		}

		@Override
		public void run() {
			Toast.makeText(_context,
					_flag == LOW_ALARM ? _lowAlarmMsg : _highAlarmMsg,
					Toast.LENGTH_SHORT).show();
		}

	}

}
