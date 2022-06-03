package com.example.beermanager.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import com.example.beermanager.MainActivity
import java.io.IOException
import java.lang.System.out

class BitmapProcessor {
    /**
     * Saves current canvas state to bitmap file.
     */
    fun saveCanvasToBitmap(width: Int, height: Int, canvas: Canvas, bitmap: Bitmap){
        try{
            canvas.setBitmap(bitmap);
            val r = Runnable {
                bitmap.compress(
                    Bitmap.CompressFormat.PNG, 100, MainActivity.fileContext?.openFileOutput(
                        "lastStateOfCanvas.png",
                        Context.MODE_PRIVATE
                    )
                );
            }
            val t = Thread(r)
            t.start()
        }
        catch(e: IOException){
            out.println(e.stackTrace)
        }
    }

    /**
     * Draws bitmap to canvas.
     */
    fun loadBitmapToCanvas(canvas:Canvas, lastBitmapOfPreviousRun: Bitmap?, paint: Paint) : Bitmap?{
        try{
            var bitmapData: ByteArray = ByteArray(0)
            var newLastBitmapOfPreviousRun = lastBitmapOfPreviousRun;
            if(newLastBitmapOfPreviousRun==null) {

                MainActivity.fileContext?.openFileInput("lastStateOfCanvas.png").use { fis ->
                    if (fis != null) {
                        bitmapData = fis.readBytes()
                    }
                }
                newLastBitmapOfPreviousRun =  BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.count());
            }
            if(newLastBitmapOfPreviousRun!=null)
                canvas.drawBitmap(newLastBitmapOfPreviousRun as Bitmap,0F,0F, paint)
            return newLastBitmapOfPreviousRun
        }
        catch(e:IOException){
            out.println(e.stackTrace)
            return null
        }

    }
}