package com.example.auditshield.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.auditshield.database.entity.QuestionEntity
import com.example.auditshield.ui.screen.AuditDetailScreen
import com.example.auditshield.ui.screen.AuditListScreen
import com.example.auditshield.ui.screen.AuditQuestionsScreen
import com.example.auditshield.ui.screen.AuditReportScreen
import com.example.auditshield.ui.screen.HomeScreen
import com.example.auditshield.viewmodel.AuditViewModel
import com.example.auditshield.viewmodel.HomeViewModel

sealed class Screen(val route: String) {
    object Home: Screen("home")
    object AuditList: Screen("audit_list")
    object Dashboard: Screen("dashboard")
    object AuditDetail: Screen("audit_detail/{auditId}") {
        fun createRoute(id: Long) = "audit_detail/$id"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = vm,
                onOpenAuditList = { navController.navigate(Screen.AuditList.route) },
                onOpenAudit = { id -> navController.navigate(Screen.AuditDetail.createRoute(id)) },
                onOpenDashboard = { navController.navigate(Screen.Dashboard.route) }
            )
        }
        composable(Screen.Dashboard.route) {
            com.example.auditshield.ui.screen.DashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "audit_questions/{auditId}",
            arguments = listOf(navArgument("auditId") { type = NavType.LongType })
        ) { backStackEntry ->
            val auditId = backStackEntry.arguments?.getLong("auditId") ?: 0L
            AuditQuestionsScreen(navController = navController, auditId = auditId)
        }

        composable("report/{auditId}") { backStack ->
            val auditId = backStack.arguments?.getString("auditId")?.toLongOrNull() ?: return@composable
            val vm: AuditViewModel = hiltViewModel()

            LaunchedEffect(auditId) {
                vm.loadAudit(auditId)
                vm.loadQuestions()
            }

            val questions by vm.questions.collectAsState()
            val answers by vm.answers.collectAsState()
            val audit by vm.selectedAudit.collectAsState()

            if (audit == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AuditReportScreen(
                    questions = questions,
                    answers = answers,
                    totalScore = audit!!.score,
                    onBack = { navController.popBackStack() }
                )
            }
        }


        composable(Screen.AuditList.route) {
            val vm: AuditViewModel = hiltViewModel()

            AuditListScreen(
                viewModel = vm,
                onAuditClick = { id ->
                    navController.navigate(Screen.AuditDetail.createRoute(id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


        composable(Screen.AuditDetail.route) { back ->
            val id = back.arguments?.getString("auditId")?.toLongOrNull() ?: return@composable
            val vm: AuditViewModel = hiltViewModel()

            LaunchedEffect(id) {
                vm.loadAudit(id)
            }

            AuditDetailScreen(
                viewModel = vm,
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}
