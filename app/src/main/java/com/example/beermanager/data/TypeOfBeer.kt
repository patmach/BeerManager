package com.example.beermanager.data

enum class TypeOfBeer{
    NONALCOHOLIC{
        override fun toString()= "NON-ALCOHOLIC"
        override fun getAlcoholPercentage(): Double {
            return 0.0
        }
    }
    ,TEN{
        override fun toString()= "10°"
        override fun getAlcoholPercentage(): Double {
            return 4.2/100
        }
    }
    , ELEVEN{
        override fun toString()= "11°"
        override fun getAlcoholPercentage(): Double {
            return 4.7/100
        }
    }
    , TWELVE{
        override fun toString()= "12°"
        override fun getAlcoholPercentage(): Double {
            return 5.1/100
        }
    }
    , THIRTEEN{
        override fun toString()= "13°"
        override fun getAlcoholPercentage(): Double {
            return 5.7/100
        }
    };
    abstract override fun toString(): String
    abstract fun getAlcoholPercentage(): Double
}
