//
//  ViewController.swift
//  UrbanEvac
//
//  Created by Nasi Robinson on 2/16/17.
//  Copyright © 2017 Nas. All rights reserved.
//

import UIKit
import CoreLocation
import MapKit
import Foundation
import UserNotifications





class ViewController: UIViewController,CLLocationManagerDelegate {
    
    @IBOutlet weak var placedPin: UILabel!
   
    var locationManager = CLLocationManager()
    //var userLocation:CLLocation?
    var startLocation:CLLocation?

    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        locationManager = CLLocationManager()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestAlwaysAuthorization()
        locationManager.startUpdatingLocation()
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge], completionHandler: {didAllow, error in})
    }
  

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    @IBAction func getLocation(_ sender: Any) {
        startLocation = nil
        locationManager.startUpdatingLocation()
        
        let storyBoard : UIStoryboard = UIStoryboard(name:"Main", bundle:nil)
        
        let nextViewController = storyBoard.instantiateViewController(withIdentifier: "MapView")
        self.present(nextViewController, animated: true, completion: nil)
        
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let userLocation = locations[0]

        let uid:String = UIDevice.current.identifierForVendor!.uuidString
        let longitude:String = userLocation.coordinate.longitude.description
        let latitude:String = userLocation.coordinate.latitude.description
        
        
        
        let urls:String = "https://urbanevac.azurewebsites.net/api/users/add/\(uid)/\(latitude)/\(longitude)/"
        print(urls)
        
        
        
        if startLocation == nil {
            startLocation = userLocation
            //locationManager.stopUpdatingLocation()
        }
    
        
    }
    
    func checkRoute(){
        let content = UNMutableNotificationContent();
        content.title = "Route Blocked!"
        content.body = "New route is Calculating"
        content.badge = 1
        
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        //if blocked roads contains one in this route
            let request = UNNotificationRequest(identifier: "Route Changed", content: content, trigger: trigger)
            UNUserNotificationCenter.current().add(request, withCompletionHandler: nil)
 
    }
    


}

