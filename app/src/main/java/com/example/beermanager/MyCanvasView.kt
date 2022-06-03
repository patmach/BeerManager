package com.example.beermanager
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.beermanager.MainActivity.Companion.currentDrinkingSession
import com.example.beermanager.MainActivity.Companion.loadLastCanvas
import com.example.beermanager.MainFragment.Companion.paint
import com.example.beermanager.MainFragment.Companion.path
import com.example.beermanager.data.BitmapProcessor
import com.example.beermanager.viewmodels.MyCanvasViewModel
import com.example.beermanager.viewmodels.PriceViewModel
import java.io.IOException
import java.lang.System.out


class MyCanvasView: View{
    private val bitmapProcessor = BitmapProcessor()

    private var params : ViewGroup.LayoutParams? = null

    /**
     * Remembers x coordinate of position for start of touch sequence (MOVE DOWN)
     */
    private var eventstartX=0F

    /**
     * Remembers y coordinate of position for start of touch sequence (MOVE DOWN)
     */
    private var eventstartY=0F

    /**
     * Remembers whole path of current/last touch sequence
     */
    private var currentPathX = ArrayList<Float>()

    /**
     * Remembers whole path of current/last touch sequence
     */
    private var currentPathY = ArrayList<Float>()

    /**
    * Toast used for messages for user about validity of drawn line
    */
    private var mToast:Toast?=null

    /**
     * Flag indicating if current canvas state should be saved to bitmap file.
     */
    private var saveCanvasToBitmap=false;

    /**
     * Can contain bitmap from last run of application
     */
    private var lastBitmapOfPreviousRun:Bitmap?=null
    //companion object{
    /**
     * Contains paths of already valid drawn lines.
     */

    var pathList = ArrayList<Path>();
    /**
     * Contains path of currently drawn line.
     */
    var newPathList=ArrayList<Path>()

    /**
     * Contains paths of already invalid drawn lines.
     */
    var badPathList=ArrayList<Path>()

    /**
     * Specifies which color to use for painting lines.
     */
    var currentBrush = Color.WHITE;
    //}
    constructor(context: Context) : this(context, null){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * Constructor. Initiliaze canvas.
     */
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
        when(event.action){
            //Move started
            MotionEvent.ACTION_DOWN ->{
                currentPathX.clear()
                currentPathY.clear()
                path=Path()
                path.moveTo(x,y)
                eventstartX=x
                eventstartY=y
                return true;
            }
            //Move continues
            MotionEvent.ACTION_MOVE ->{
                currentPathX.add(x);
                currentPathY.add(y);
                path.lineTo(x,y)
                newPathList.add(path);
            }
            //Move ended
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
        //Loads canvas from previous run of app if this run continues in the same drinking activity.
        if (loadLastCanvas){
            try{
                getBitmap(canvas, lastBitmapOfPreviousRun, paint)
            }
            catch (e:IOException){

            }
        }

        //Draws line from current move of user touch.
        for (i in newPathList.indices){
            paint.color = currentBrush
            canvas.drawPath(newPathList[i], paint);
            invalidate();
        }

        //Making invalid lines invisible to user.
        for (i in badPathList.indices){
            paint.color = Color.TRANSPARENT;
            canvas.drawPath(badPathList[i], paint);
            invalidate();
        }

        //Draws already valid lines.
        for (i in pathList.indices){
            paint.color = currentBrush;
            canvas.drawPath(pathList[i], paint);
            invalidate();
        }

        if(saveCanvasToBitmap){
            saveCanvasToBitmap=false;
            setBitmap();
        }
    }

    private fun getBitmap(canvas: Canvas, lastBitmapOfPreviousRun: Bitmap?, paint: Paint) {
        this.lastBitmapOfPreviousRun = bitmapProcessor.loadBitmapToCanvas(canvas, lastBitmapOfPreviousRun,paint);
    }

    private fun setBitmap() {
        saveCanvasToBitmap=false;
        var bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
        draw(canvas);
        bitmapProcessor.saveCanvasToBitmap(this.width, this.height,canvas, bitmap);
    }


    /**
     * Checks if new drawn line is valid
     */
    private fun checkLine(x:Float,y:Float):Boolean{
        val eventendX=x
        val eventendY=y
        var heightok=false;
        var widthok = false;
        var trajectoryok=false;
        //High atleast as 1/3 of canvas height
        if(eventendY-eventstartY>this.height/3)
            heightok=true
        //Not wider than 1/5 of canvas width
        if (Math.abs((currentPathX.maxOrNull()?: Float.MAX_VALUE)-(currentPathX.minOrNull()?:Float.MIN_VALUE))<this.width/5)
            widthok=true;

        //Drawn in one direction
        if ((currentPathY.indices.all { i -> (i==currentPathY.lastIndex) || (currentPathY[i]<currentPathY[i+1])}) ||
            (currentPathY.indices.all { i -> (i==0) || (currentPathY[i]<currentPathY[i-1])})) {
            trajectoryok = true;
        }

        if (heightok && widthok && trajectoryok){
            pathList.addAll(newPathList)
            mToast?.setText("Beer added")
            saveCanvasToBitmap=true
            currentDrinkingSession.addBeer()
        }
        else {
            badPathList.addAll(newPathList)
            path=Path()
            var message=context.getString(R.string.badline)
            if(!heightok)
                message+=context.getString(R.string.badline_height)
            if(!widthok)
                message+=context.getString(R.string.badline_width)
            if(!trajectoryok)
                message+=context.getString(R.string.badline_direction)
            mToast?.setText(message)
        }
        mToast?.show()
        newPathList= ArrayList<Path>();
        return true;
    }


}