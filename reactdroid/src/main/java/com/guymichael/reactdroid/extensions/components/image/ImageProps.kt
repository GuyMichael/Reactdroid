package com.guymichael.reactdroid.extensions.components.image

import androidx.annotation.DrawableRes

data class ImageProps(
        override val remoteUrl: String?
        , @DrawableRes override val localOrOnError: Int?
        , @DrawableRes override val remotePlaceholder: Int? = localOrOnError
    ) : BaseImageProps(remoteUrl, localOrOnError, remotePlaceholder) {

    companion object {
        fun listOf(list: List<String>
            , @DrawableRes onError: Int?
            , @DrawableRes placeholder: Int)
            : List<ImageProps> {

            return list.map { url -> ImageProps(
                url
                , onError
                , placeholder
            )
            }
        }

        /** @param list of non-zero, local drawable resources */
        fun listOf(list: List<Int>): List<ImageProps> {
            return list.map { res -> ImageProps(
                null
                , res
                , null
            )
            }
        }

        /** @param pairs should have at least one non-null/non-zero member.
         * contains the remote url and/or a local res (if has remote, local res will be the 'onError' res)
         * @param placeholder for until the remote image has been loaded
         */
        fun listOf(vararg pairs: Pair<String?, Int?>, @DrawableRes placeholder: Int?): List<ImageProps> {
            return pairs.map { urlAndRes -> ImageProps(
                urlAndRes.first
                , urlAndRes.second
                , placeholder
            )
            }
        }

        /** @param remoteErrorPlaceholder should have at least one non-null member between first and second.
         * each triple contains:
         * 1. first - the remote url
         * 2. second - onError res
         * 3. third - placeholder for until the remote image has been loaded
         */
        fun listOf(vararg remoteErrorPlaceholder: Triple<String?, Int?, Int?>): List<ImageProps> {
            return remoteErrorPlaceholder.map { urlErrorPlaceholder -> ImageProps(
                urlErrorPlaceholder.first
                , urlErrorPlaceholder.second
                , urlErrorPlaceholder.third
            )
            }
        }
    }
}