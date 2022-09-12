package com.stepup.checkout.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stepup.checkout.common.urlDecode
import com.stepup.checkout.common.urlEncode

@Composable
fun CheckoutApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = start, modifier = Modifier.fillMaxSize()) {
        composable(start) {
            CheckoutScreen(
                verify = {
                    navController.navigate("$webPage?$url=${it.urlEncode()}")
                }
            )
        }
        composable(
            "$webPage?$url={$url}",
            arguments = listOf(navArgument(url) { type = NavType.StringType })
        ) {
            VerificationWebPage(
                verifyUrl = it.arguments?.getString(url)?.urlDecode()
                    ?: error("URL not provided"),
                onResult = {
                    navController.navigate("$result?$success=$it") {
                        popUpTo(start) { inclusive = true }
                    }
                }
            )
        }
        composable(
            "$result?$success={$success}",
            arguments = listOf(navArgument(success) { type = NavType.BoolType })
        ) {
            ResultScreen(
                success = it.arguments?.getBoolean(success, true) ?: true,
                restart = {
                    navController.navigate(start) {
                        popUpTo(it.destination.route!!) { inclusive = true }
                    }
                }
            )
        }
    }
}

private const val start = "start"
private const val result = "result"
private const val webPage = "webPage"
private const val success = "success"
private const val url = "url"