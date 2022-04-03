package com.example.beermanager.data

/**
 * Contains different types of beer.
 */
enum class TypeOfBeer{
    NONALCOHOLIC{
        override fun toStringWithNumber()= "NON-ALCOHOLIC"
        override fun getAlcoholPercentage(): Double {
            return 0.0
        }
    }
    ,TEN{
        override fun toStringWithNumber()= "10°"
        override fun getAlcoholPercentage(): Double {
            return 4.0/100
        }
    }
    , ELEVEN{
        override fun toStringWithNumber()= "11°"
        override fun getAlcoholPercentage(): Double {
            return 4.7/100
        }
    }
    , TWELVE{
        override fun toStringWithNumber()= "12°"
        override fun getAlcoholPercentage(): Double {
            return 5.1/100
        }
    }
    , THIRTEEN{
        override fun toStringWithNumber()= "13°"
        override fun getAlcoholPercentage(): Double {
            return 5.6/100
        }
    };

    /**
     * @return String representation of the value that is shown to the user.
     */
    abstract  fun toStringWithNumber(): String

    /**
     * @return Estimated percentage of alcohol in particular type of beer.
     */
    abstract fun getAlcoholPercentage(): Double
}
