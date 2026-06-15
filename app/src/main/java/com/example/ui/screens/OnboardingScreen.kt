package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EcoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: EcoViewModel, onComplete: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EcoTrace AI",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress tracker
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { (viewModel.onboardingIndex + 1) / 7f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("onboarding_progress"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Question ${viewModel.onboardingIndex + 1} of 7",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "One minute setup • AI interprets your choices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Question Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = viewModel.onboardingIndex,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "QuestionAnimation"
                ) { targetIndex ->
                    when (targetIndex) {
                        0 -> TransportQuestion(
                            selectedType = viewModel.obTransportType,
                            onTypeSelected = { viewModel.obTransportType = it },
                            distance = viewModel.obTransportDistance,
                            onDistanceChanged = { viewModel.obTransportDistance = it }
                        )

                        1 -> EnergyQuestion(
                            selectedHabit = viewModel.obEnergyHabit,
                            onHabitSelected = { viewModel.obEnergyHabit = it },
                            usage = viewModel.obEnergyUsage,
                            onUsageChanged = { viewModel.obEnergyUsage = it }
                        )

                        2 -> DietQuestion(
                            selectedDiet = viewModel.obDietType,
                            onDietSelected = { viewModel.obDietType = it }
                        )

                        3 -> FlightsQuestion(
                            selectedFreq = viewModel.obFlightFreq,
                            onFreqSelected = { viewModel.obFlightFreq = it }
                        )

                        4 -> ShoppingQuestion(
                            selectedShopping = viewModel.obShoppingFreq,
                            onShoppingSelected = { viewModel.obShoppingFreq = it }
                        )

                        5 -> WasteQuestion(
                            selectedWaste = viewModel.obWasteHabit,
                            onWasteSelected = { viewModel.obWasteHabit = it }
                        )

                        6 -> HouseholdQuestion(
                            size = viewModel.obHouseholdSize,
                            onSizeChanged = { viewModel.obHouseholdSize = it }
                        )
                    }
                }
            }

            // Navigation Row at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                if (viewModel.onboardingIndex > 0) {
                    FilledTonalButton(
                        onClick = { viewModel.onboardingIndex-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("onboarding_back"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // Next/Finish Button
                Button(
                    onClick = {
                        if (viewModel.onboardingIndex < 6) {
                            viewModel.onboardingIndex++
                        } else {
                            viewModel.completeOnboardingWorkflow()
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp)
                        .testTag("onboarding_next"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (viewModel.onboardingIndex == 6) "Calculate Footprint ✨" else "Next Question",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (viewModel.onboardingIndex == 6) Icons.Default.Done else Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

// Q1: Transportation
@Composable
fun TransportQuestion(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    distance: String,
    onDistanceChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "What is your main transport option?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Select a general baseline habit below or type your exact commute distance.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Large touch options
        val transportOptions = listOf(
            Triple("Walk", "Walk / Cycle", Icons.Default.DirectionsWalk),
            Triple("Bus", "Public Bus", Icons.Default.DirectionsBus),
            Triple("Train", "Train / Metro", Icons.Default.DirectionsSubway),
            Triple("Motorcycle", "Motorbike", Icons.Default.TwoWheeler),
            Triple("Car", "Personal Car", Icons.Default.DirectionsCar)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            transportOptions.forEach { (type, label, icon) ->
                SelectableOptionRow(
                    isSelected = selectedType.lowercase() == type.lowercase(),
                    label = label,
                    icon = icon,
                    onClick = { onTypeSelected(type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Direct Exact Input
        OutlinedTextField(
            value = distance,
            onValueChange = onDistanceChanged,
            label = { Text("Or enter exact weekly distance (Optional)") },
            placeholder = { Text("e.g. 80 km per week") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_input_transport"),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.AddLocation, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
        )
    }
}

// Q2: Home Energy
@Composable
fun EnergyQuestion(
    selectedHabit: String,
    onHabitSelected: (String) -> Unit,
    usage: String,
    onUsageChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "How much electricity does your home use?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Estimate roughly or enter exact utility records inside the text box.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        val energyOptions = listOf(
            Triple("Low", "Low Usage (Small flat, conscious use)", Icons.Default.LightbulbCircle),
            Triple("Average", "Average Usage (Medium home, standard uses)", Icons.Default.ElectricBolt),
            Triple("High", "High Usage (AC/Heaters on often, big house)", Icons.Default.FlashOn)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            energyOptions.forEach { (habit, label, icon) ->
                SelectableOptionRow(
                    isSelected = selectedHabit.lowercase() == habit.lowercase(),
                    label = label,
                    icon = icon,
                    onClick = { onHabitSelected(habit) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = usage,
            onValueChange = onUsageChanged,
            label = { Text("Or enter monthly utility bills (Optional)") },
            placeholder = { Text("e.g. 250 kWh per month") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_input_energy"),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
        )
    }
}

// Q3: Diet
@Composable
fun DietQuestion(selectedDiet: String, onDietSelected: (String) -> Unit) {
    Column {
        Text(
            text = "What best describes your general diet?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Diet is a major generator of household green house emissions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        val dietOptions = listOf(
            Triple("Vegan", "100% Plant-Based (Vegan 🥗)", Icons.Default.Spa),
            Triple("Vegetarian", "Vegetarian (No meats 🥚)", Icons.Default.EnergySavingsLeaf),
            Triple("Mixed", "Balanced Mixed (Veggies & occasional meat 🍕)", Icons.Default.Restaurant),
            Triple("Meat-heavy", "High Meat (Frequent beef & poultry 🥩)", Icons.Default.SetMeal)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            dietOptions.forEach { (diet, label, icon) ->
                SelectableOptionRow(
                    isSelected = selectedDiet.lowercase() == diet.lowercase(),
                    label = label,
                    icon = icon,
                    onClick = { onDietSelected(diet) }
                )
            }
        }
    }
}

// Q4: Flights
@Composable
fun FlightsQuestion(selectedFreq: String, onFreqSelected: (String) -> Unit) {
    Column {
        Text(
            text = "How often do you travel by flight?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Air travel adds massive personal carbon spikes instantly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        val flightOptions = listOf(
            Triple("None", "No Trips (I bypass flying entirely 🚌)", Icons.Default.Block),
            Triple("Rare", "Rare (1-2 domestic flights a year ✈️)", Icons.Default.FlightTakeoff),
            Triple("Frequent", "Frequent (Multiple international or business trips 🛫)", Icons.Default.FlightLand)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            flightOptions.forEach { (freq, label, icon) ->
                SelectableOptionRow(
                    isSelected = selectedFreq.lowercase() == freq.lowercase(),
                    label = label,
                    icon = icon,
                    onClick = { onFreqSelected(freq) }
                )
            }
        }
    }
}

// Q5: Shopping
@Composable
fun ShoppingQuestion(selectedShopping: String, onShoppingSelected: (String) -> Unit) {
    Column {
        Text(
            text = "What are your shopping habits?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Refining packaging and fashion buys cuts manufacturing carbon.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        val shoppingOptions = listOf(
            Triple("Minimalist", "Conscious (Buy mostly secondhand, only essentials 🛍️)", Icons.Default.FilterList),
            Triple("Average", "Standard (Occasional tech and fashion purchases 💳)", Icons.Default.ShoppingBag),
            Triple("Shopper", "Frequent (Passionate browser, buying new releases 🏷️)", Icons.Default.Storefront)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            shoppingOptions.forEach { (shopping, label, icon) ->
                SelectableOptionRow(
                    isSelected = selectedShopping.lowercase() == shopping.lowercase(),
                    label = label,
                    icon = icon,
                    onClick = { onShoppingSelected(shopping) }
                )
            }
        }
    }
}

// Q6: Waste
@Composable
fun WasteQuestion(selectedWaste: String, onWasteSelected: (String) -> Unit) {
    Column {
        Text(
            text = "What are your waste habits?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Recycling, composting, and avoiding plastic curbs methane output.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        val wasteOptions = listOf(
            Triple("Low", "Zero Waste Hero (Thorough recycle & compost ♻️)", Icons.Default.Recycling),
            Triple("Average", "Balanced (Try to recycle when convenient 🗑️)", Icons.Default.DeleteOutline),
            Triple("High", "Standard (Throw items together, no compost 🦖)", Icons.Default.Delete)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            wasteOptions.forEach { (waste, label, icon) ->
                SelectableOptionRow(
                    isSelected = selectedWaste.lowercase() == waste.lowercase(),
                    label = label,
                    icon = icon,
                    onClick = { onWasteSelected(waste) }
                )
            }
        }
    }
}

// Q7: Household Size
@Composable
fun HouseholdQuestion(size: Int, onSizeChanged: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "How many members share your household?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Electricity and heating carbon is shared. Living together reduces personal footprints.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "$size",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "member" + if (size > 1) "s" else "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Slider(
            value = size.toFloat(),
            onValueChange = { onSizeChanged(it.toInt()) },
            valueRange = 1f..6f,
            steps = 4,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .testTag("onboarding_input_household")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..6).forEach { num ->
                Text(
                    text = if (num == 6) "6+" else "$num",
                    fontWeight = FontWeight.Bold,
                    color = if (size == num) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onSizeChanged(num) }
                )
            }
        }
    }
}

// Selectable Helper Card
@Composable
fun SelectableOptionRow(
    isSelected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .testTag("option_${label.replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) BorderStroke(1.5.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
