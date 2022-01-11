package com.example.beermanager
import android.R.attr
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.beermanager.MainActivity.Companion.currentDrinkingActivity
import com.example.beermanager.MainActivity.Companion.loadLastCanvas
import com.example.beermanager.SecondFragment.Companion.paint
import com.example.beermanager.SecondFragment.Companion.path
import com.example.beermanager.data.DrinkingActivity
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.lang.System.out
import android.R.attr.bitmap




class MyCanvasView: View{
    var params : ViewGroup.LayoutParams? = null
    private var eventstartX=0F
    private var eventstartY=0F
    private var currentPathX = ArrayList<Float>()
    private var currentPathY = ArrayList<Float>()
    private var mToast:Toast?=null
    private var saveCanvasToBitmap=false;
    private var lastBitmapOfPreviousRun:Bitmap?=null
    companion object{
        var pathList = ArrayList<Path>();
        var newPathList=ArrayList<Path>()
        var badPathList=ArrayList<Path>()
        var currentBrush = Color.WHITE;
    }
    constructor(context: Context) : this(context, null){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    private fun init(){
        mToast= Toast.makeText(context, "", Toast.LENGTH_LONG);
        paint.isAntiAlias =true;
        paint.color= currentBrush;
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 15F
        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return true;
        var x = event.x;
        var y = event.y
        var lastpath=Path()
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                currentPathX.clear()
                currentPathY.clear()
                path=Path()
                path.moveTo(x,y)
                eventstartX=x
                eventstartY=y
                return true;
            }
            MotionEvent.ACTION_MOVE ->{
                currentPathX.add(x);
                currentPathY.add(y);
                path.lineTo(x,y)
                newPathList.add(path);
            }
            MotionEvent.ACTION_UP->{
                return checkLine(x,y)
            }
            else -> return true;
        }
        postInvalidate();
        return false;

    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas==null)
            return;
        if (loadLastCanvas){
            try{
                loadCanvasToBitmap(canvas)
            }
            catch (e:IOException){

            }
        }

        for (i in newPathList.indices){
            paint.color = currentBrush
            canvas.drawPath(newPathList[i], paint);
            invalidate();
        }
        for (i in badPathList.indices){
            paint.color = Color.TRANSPARENT;
            canvas.drawPath(badPathList[i], paint);
            invalidate();
        }
        for (i in pathList.indices){
            paint.color = currentBrush;
            canvas.drawPath(pathList[i], paint);
            invalidate();
        }
        if(saveCanvasToBitmap){
            saveCanvasToBitmap()
        }
    }

    fun saveCanvasToBitmap(){
        try{
            saveCanvasToBitmap=false
            var bitmap = Bitmap.createBitmap(this.width,this.height,Bitmap.Config.ARGB_8888);
            val canvas = Canvas(bitmap)
            draw(canvas)
            canvas.setBitmap(bitmap);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, MainActivity.fileContext?.openFileOutput("lastStateOfCanvas.png",
                Context.MODE_PRIVATE));

        }
        catch(e:IOException){
            out.println(e.stackTrace)
        }
    }

    fun loadCanvasToBitmap(canvas:Canvas){
        try{
            var bitmapData: ByteArray = ByteArray(0)
            if(lastBitmapOfPreviousRun==null) {

                MainActivity.fileContext?.openFileInput("lastStateOfCanvas.png").use { fis ->
                    if (fis != null) {
                        bitmapData = fis.readBytes()
                    }
                }
                lastBitmapOfPreviousRun =  BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.count());
            }
            if(lastBitmapOfPreviousRun!=null)
                canvas.drawBitmap(lastBitmapOfPreviousRun as Bitmap,0F,0F, paint)

        }
        catch(e:IOException){
            out.println(e.stackTrace)
        }
    }

    private fun checkLine(x:Float,y:Float):Boolean{
        val eventendX=x
        val eventendY=y
        var heightok=false;
        var widthok = false;
        var trajectoryok=false;
        if(eventendY-eventstartY>this.height/3)
            heightok=true
        if (Math.abs((currentPathX.maxOrNull()?: Float.MAX_VALUE)-(currentPathX.minOrNull()?:Float.MIN_VALUE))<this.width/5)
            widthok=true;
        if ((currentPathY.indices.all { i -> (i==currentPathY.lastIndex) || (currentPathY[i]<currentPathY[i+1])}) ||
            (currentPathY.indices.all { i -> (i==0) || (currentPathY[i]<currentPathY[i-1])})) {
            trajectoryok = true;
        }
        if (heightok && widthok && trajectoryok){
            pathList.addAll(newPathList)
            mToast?.setText("Beer added")
            saveCanvasToBitmap=true
            currentDrinkingActivity.addBeer()
        }
        else {
            //Log.i("Info","bad line")
            badPathList.addAll(newPathList)
            path=Path()
            var message="BAD LINE!\n"
            if(!heightok)
                message+="Your line has to be at least as long as 1/3 of the frame height. "
            if(!widthok)
                message+="Your line has to be at least as narrow as 1/5 of the frame width. "
            if(!trajectoryok)
                message+="Your line has to be drawn in one direction (top to bottom or bottom to top)."
            mToast?.setText(message)
        }
        mToast?.show()
        newPathList= ArrayList<Path>();
        return true;
    }


}