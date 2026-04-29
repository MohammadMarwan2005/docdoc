package com.alaishat.mohammad.docdoc.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.alaishat.mohammad.docdoc.R
import com.alaishat.mohammad.docdoc.errohandling.ErrorHandler
import com.alaishat.mohammad.docdoc.navigaiton.DoctorDetailsDestination
import com.alaishat.mohammad.docdoc.resuables.DocDocButton
import com.alaishat.mohammad.docdoc.resuables.DocDocTopAppBar
import com.alaishat.mohammad.docdoc.resuables.DoctorCard
import com.alaishat.mohammad.docdoc.ui.theme.DocDocSurface
import com.alaishat.mohammad.docdoc.ui.theme.DocDocUnselectedChipContainer
import com.alaishat.mohammad.docdoc.ui.theme.DocDocUnselectedChipLabel
import com.alaishat.mohammad.docdoc.ui.theme.Seed
import com.alaishat.mohammad.docdoc.vm.SearchViewModel
import com.alaishat.mohammad.domain.model.all_specialization.response.AllSpecializationsResponse
import com.alaishat.mohammad.domain.model.all_specialization.response.success.AllSpecializationSuccessResponse
import com.alaishat.mohammad.domain.model.all_specialization.response.success.SpecializationsFullData
import com.alaishat.mohammad.domain.model.core.Doctor
import com.alaishat.mohammad.domain.model.search.DoctorSearchResultSuccessResponse
import com.alaishat.mohammad.domain.usecase.DocDocSearchFilter
import kotlinx.coroutines.launch

/**
 * Created by Mohammad Al-Aishat on Aug/31/2024.
 * DocDoc Project.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    navController: NavHostController,
    onSnackBarHostEvent: (String) -> Unit
) {
    searchViewModel.getAllSpecializationsResponse()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var allSelected by rememberSaveable {
        mutableStateOf(false)
    }

    val allSpecializationsDadResponse: AllSpecializationsResponse? =
        searchViewModel.allSpecializationsResponse.collectAsStateWithLifecycle().value


    val dadSearchResult = searchViewModel.searchResultResponse.collectAsStateWithLifecycle().value
    val dadSearchLoading = searchViewModel.isLoading.collectAsStateWithLifecycle().value


    val allSpecs: List<SpecializationsFullData>? =
        (allSpecializationsDadResponse as? AllSpecializationSuccessResponse)?.data
    val foundDoctors: List<Doctor>? = (dadSearchResult as? DoctorSearchResultSuccessResponse)?.data


    val selectedIds = remember { mutableStateListOf<Int>() }


    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
//        sheetPeekHeight = 0.dp,
//        containerColor = MaterialTheme.colorScheme.background,
//        sheetContainerColor = MaterialTheme.colorScheme.background,
        sheetContent = {
            Scaffold(
                modifier = Modifier.padding(24.dp),
                bottomBar = {
                    DocDocButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            coroutineScope.launch { scaffoldState.bottomSheetState.hide() }
                        }) {
                        Text(
                            modifier = Modifier.padding(14.dp),
                            text = stringResource(R.string.done)
                        )
                    }
                }
            ) { paddingValues ->

                Column(
                    modifier = Modifier.padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.filtered_by),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Divider()

                    allSpecs?.let {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item {
                                Box(contentAlignment = Alignment.Center) {
                                    FilterChip(selected = allSelected, onClick = {
                                        allSelected = !allSelected
                                        if (allSelected) {
                                            selectedIds.clear()
                                            selectedIds.addAll(allSpecs.map { it.id })
                                        }
                                    }, label = {
                                        Text(
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                            style = MaterialTheme.typography.titleMedium,
                                            text = "All",
                                        )
                                    },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DocDocUnselectedChipContainer,
                                            labelColor = DocDocUnselectedChipLabel,
                                            selectedContainerColor = Seed,
                                            selectedLabelColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderWidth = 0.dp,
                                            selectedBorderWidth = 0.dp
                                        )
                                    )

                                }
                            }
                            items(it, key = { spec -> spec.id }) { spec ->

                                Box(contentAlignment = Alignment.Center) {
                                    FilterChip(selected = selectedIds.contains(spec.id), onClick = {
                                        if (selectedIds.contains(spec.id)) selectedIds.remove(spec.id)
                                        else selectedIds.add(spec.id)
                                    }, label = {
                                        Text(
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                            style = MaterialTheme.typography.titleMedium,
                                            text = spec.name
                                        )
                                    },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = DocDocUnselectedChipContainer,
                                            labelColor = DocDocUnselectedChipLabel,
                                            selectedContainerColor = Seed,
                                            selectedLabelColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderWidth = 0.dp,
                                            selectedBorderWidth = 0.dp
                                        )
                                    )

                                }

                            }

                        }
                    } ?: Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {CircularProgressIndicator()}
                }
            }
        }) {
        Scaffold(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 16.dp),
            topBar = {
                DocDocTopAppBar(
                    navController = navController,
                    text = stringResource(R.string.search),
                    trailingIcon = { Spacer(modifier = Modifier.width(32.dp)) })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    DockedSearchBar(
                        modifier = Modifier.weight(9f),
                        trailingIcon = {
                            IconButton(onClick = {
                                searchViewModel.search(
                                    searchQuery,
                                    filter = DocDocSearchFilter(selectedIds)
                                )
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_search),
                                    contentDescription = "",
                                    tint = Color.Gray
                                )
                            }
                        },
                        placeholder = { Text(text = stringResource(id = R.string.search), color = Color.Gray) },
                        colors = SearchBarDefaults.colors(containerColor = DocDocSurface),
                        query = searchQuery, onQueryChange = { searchQuery = it }, onSearch = { query ->
                            searchViewModel.search(query, filter = DocDocSearchFilter(selectedIds))
                        }, active = false, onActiveChange = {}) {}

                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }) {
                        Icon(painter = painterResource(id = R.drawable.ic_filter_results), contentDescription = "")
                    }
                }



                allSpecs?.let { allSpecs: List<SpecializationsFullData> ->

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            FilterChip(selected = allSelected, onClick = {
                                allSelected = !allSelected
                                if (allSelected) {
                                    selectedIds.clear()
                                    selectedIds.addAll(allSpecs.map { it.id })
                                }
                            }, label = {
                                Text(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    text = stringResource(R.string.all)
                                )
                            },
                                shape = RoundedCornerShape(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DocDocUnselectedChipContainer,
                                    labelColor = DocDocUnselectedChipLabel,
                                    selectedContainerColor = Seed,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderWidth = 0.dp,
                                    selectedBorderWidth = 0.dp
                                )
                            )
                        }
                        items(allSpecs, key = { spec -> spec.id }) { spec ->
                            FilterChip(selected = selectedIds.contains(spec.id), onClick = {
                                if (selectedIds.contains(spec.id)) selectedIds.remove(spec.id)
                                else selectedIds.add(spec.id)
                            }, label = {
                                Text(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    text = spec.name
                                )
                            },
                                shape = RoundedCornerShape(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DocDocUnselectedChipContainer,
                                    labelColor = DocDocUnselectedChipLabel,
                                    selectedContainerColor = Seed,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderWidth = 0.dp,
                                    selectedBorderWidth = 0.dp
                                )
                            )

                        }
                    }
                } ?: CircularProgressIndicator()


                if (dadSearchLoading) CircularProgressIndicator()

                foundDoctors?.let { doctorsData ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.founds, doctorsData.size),
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn {
                        items(doctorsData) { doctor ->
                            DoctorCard(
                                name = doctor.name,
                                specialization = doctor.specialization.name,
                                city = doctor.city.name,
                                model = doctor.photo,
                                degree = doctor.degree,
                                phone = doctor.phone,
                                onClick = {
                                    navController.navigate(DoctorDetailsDestination.route + "/${doctor.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = dadSearchResult) {
            ErrorHandler.handle(
            dadSearchResult,
            onResetState = { searchViewModel.resetSearchResultState() },
            onSnackBarHostEvent = { onSnackBarHostEvent(it) }
        )
    }

    LaunchedEffect(key1 = allSpecializationsDadResponse) {
        ErrorHandler.handle(
            allSpecializationsDadResponse,
            onResetState = { searchViewModel.resetAllSpecializations() },
            onSnackBarHostEvent = { onSnackBarHostEvent(it) }
        )
    }
}
