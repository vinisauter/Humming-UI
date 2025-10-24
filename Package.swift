// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "HummingUI",
    platforms: [
        .iOS(.v13), .macOS(.v11)
    ],
    products: [
        .library(name: "HummingCore", targets: ["HummingCore"]),
        .library(name: "HummingActions", targets: ["HummingActions"]),
        .library(name: "HummingNavigation", targets: ["HummingNavigation"]),
        .library(name: "HummingLayoutMaterial3", targets: ["HummingLayoutMaterial3"])
    ],
    targets: [
        .binaryTarget(
            name: "HummingCore",
            url: "https://github.com/vinisauter/Humming-UI/releases/download/${VERSION}/HummingCore.xcframework.zip",
            checksum: "${HUMMING_CORE_CHECKSUM}"
        ),
        .binaryTarget(
            name: "HummingActions",
            url: "https://github.com/vinisauter/Humming-UI/releases/download/${VERSION}/HummingActions.xcframework.zip",
            checksum: "${HUMMING_ACTIONS_CHECKSUM}"
        ),
        .binaryTarget(
            name: "HummingNavigation",
            url: "https://github.com/vinisauter/Humming-UI/releases/download/${VERSION}/HummingNavigation.xcframework.zip",
            checksum: "${HUMMING_NAVIGATION_CHECKSUM}"
        ),
        .binaryTarget(
            name: "HummingLayoutMaterial3",
            url: "https://github.com/vinisauter/Humming-UI/releases/download/${VERSION}/HummingLayoutMaterial3.xcframework.zip",
            checksum: "${HUMMING_LAYOUT_MATERIAL3_CHECKSUM}"
        )
    ]
)

