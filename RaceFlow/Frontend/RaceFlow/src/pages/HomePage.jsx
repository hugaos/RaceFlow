import { useState } from 'react'
import Navbar from '../components/navbar'
import DriverTable from '../components/driverTable'
import F1Track from '../components/F1track'
import '../css/index.css'


function App() {
    return (
        <div className="page">
            <Navbar />
            <div className="container">
                <div className="row">
                    <div className="table-column">   
                        <DriverTable />
                    </div>
                    <div className="track-col">
                        <F1Track />
                    </div>
                </div>
            </div>
        </div>
    )
}

export default App
